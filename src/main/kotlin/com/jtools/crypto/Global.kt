package com.jtools.crypto

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
@State(name = "jtools-crypto", storages = [Storage(value = "JToolsCrypto.xml")])
class Global : PersistentStateComponent<GlobalState> {

    private var state: GlobalState = GlobalState()

    companion object {
        fun getInstance(project: Project): GlobalState = project.getService(Global::class.java).state
//        fun getInstance(project: Project): GlobalState = GlobalState()
    }

    override fun getState(): GlobalState = state

    override fun loadState(state: GlobalState) {
        this.state = state
    }

}

class GlobalState {
    //解密配置
    var crypto: String = """
                    - key: asdtixla146sxaz5
                      type: AES_CBC
                      iv: axksleitlglxlakz
                      name: 测试AES_CBC
                      encryptType: base64
                    - key: 126ax5f6g4564a2x
                      type: AES_ECB
                      name: 测试AES_ECB
                      encryptType: hex
                    - key: |
                        axksagfksldfnaskldgvncklsdaasd
                        asdfkjlxcvaksldfjosdigjasdgsdf
                      type: RSA_PUBLIC
                      name: 测试_RSA_PUBLIC
                      encryptType: base64
                    - key: |
                        asdfkjlcvkmasdoifgjasdglkjsdl
                        asdfkjlkvasdklgjsdlagk
                      type: RSA_PRIVATE
                      name: 测试_RSA_PRIVATE
                      encryptType: hex
                """.trimIndent()

    var selectCrypto: String = ""


    fun selectCryptoInstance(): Crypto? = if (selectCrypto.isNotEmpty()) {
        ObjectMapper().readValue(selectCrypto, Crypto::class.java)
    } else {
        null
    }

    fun cryptoList(): List<Crypto> = if (crypto.isNotEmpty()) {
        ObjectMapper().let {
            val json = it.writeValueAsString(Yaml().loadAs(crypto, List::class.java).toList())
            it.readValue(json, object : TypeReference<List<Crypto>>() {})
        }
    } else {
        listOf()
    }
}

class Crypto {
    var key: String? = ""
    var iv: String? = ""
    var type: String? = ""

    //base64 hex
    var encryptType: String = "base64"
    var name: String? = ""
    override fun toString(): String {
        return "Decrypt(key=$key, iv=$iv, type=$type, name=$name)"
    }

    fun decrypt(rawBytes: ByteArray): String {
        when (this.type) {
            "AES_CBC" -> {

                val cipher = Cipher.getInstance("AES/CBC/Pkcs5Padding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(this.key?.toByteArray(StandardCharsets.UTF_8), "AES"),
                    IvParameterSpec(this.iv?.toByteArray(StandardCharsets.UTF_8))
                )
                return String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
            }

            "AES_ECB" -> {
                val cipher = Cipher.getInstance("AES/ECB/Pkcs5Padding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(this.key?.toByteArray(StandardCharsets.UTF_8), "AES"))
                return String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
            }

            "RSA_PUBLIC" -> {
                this.key?.let {
                    X509EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                        KeyFactory.getInstance("RSA")?.generatePublic(key)
                    }
                }?.let {
                    Cipher.getInstance("RSA")?.let { rsa ->
                        rsa.init(Cipher.DECRYPT_MODE, it)
                        return String(rsa.doFinal(rawBytes), StandardCharsets.UTF_8)
                    }
                }
            }

            "RSA_PRIVATE" -> {
                this.key?.let {
                    PKCS8EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                        KeyFactory.getInstance("RSA")?.generatePrivate(key)
                    }
                }?.let {
                    Cipher.getInstance("RSA")?.let { rsa ->
                        rsa.init(Cipher.DECRYPT_MODE, it)
                        return String(rsa.doFinal(rawBytes), StandardCharsets.UTF_8)
                    }
                }
            }
        }
        return "解密失败"
    }

    fun encrypt(rawBytes: ByteArray): String {
        try{
            when (this.type) {
                "AES_CBC" -> {
                    val cipher = Cipher.getInstance("AES/CBC/Pkcs5Padding")
                    cipher.init(
                        Cipher.ENCRYPT_MODE, SecretKeySpec(
                            this.key?.toByteArray(
                                StandardCharsets.UTF_8
                            ), "AES"
                        ),
                        IvParameterSpec(this.iv?.toByteArray(StandardCharsets.UTF_8))
                    )
                    return if (encryptType == "base64") {
                        Base64.encodeBase64String(cipher.doFinal(rawBytes))
                    } else {
                        Hex.encodeHexString(cipher.doFinal(rawBytes))
                    }
                }

                "AES_ECB" -> {
                    val cipher = Cipher.getInstance("AES/ECB/Pkcs5Padding")
                    cipher.init(
                        Cipher.ENCRYPT_MODE, SecretKeySpec(
                            this.key?.toByteArray(
                                StandardCharsets.UTF_8
                            ), "AES"
                        )
                    )
                    return if (encryptType == "base64") {
                        Base64.encodeBase64String(cipher.doFinal(rawBytes))
                    } else {
                        Hex.encodeHexString(cipher.doFinal(rawBytes))
                    }
                }

                "RSA_PUBLIC" -> {
                    this.key?.let {
                        X509EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                            KeyFactory.getInstance("RSA")?.generatePublic(key)
                        }
                    }?.let {
                        Cipher.getInstance("RSA")?.let { rsa ->
                            rsa.init(Cipher.ENCRYPT_MODE, it)
                            return if (encryptType == "base64") {
                                Base64.encodeBase64String(rsa.doFinal(rawBytes))
                            } else {
                                Hex.encodeHexString(rsa.doFinal(rawBytes))
                            }
                        }
                    }
                }

                "RSA_PRIVATE" -> {
                    this.key?.let {
                        PKCS8EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                            KeyFactory.getInstance("RSA")?.generatePrivate(key)
                        }
                    }?.let {
                        Cipher.getInstance("RSA")?.let { rsa ->
                            rsa.init(Cipher.ENCRYPT_MODE, it)
                            return if (encryptType == "base64") {
                                Base64.encodeBase64String(rsa.doFinal(rawBytes))
                            } else {
                                Hex.encodeHexString(rsa.doFinal(rawBytes))
                            }
                        }
                    }
                }
            }
        }catch (ignore:Throwable){

        }
        return "加密失败"
    }


    fun toJson(): String = ObjectMapper().writeValueAsString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Crypto

        if (key != other.key) return false
        if (iv != other.iv) return false
        if (type != other.type) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (iv?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }


}