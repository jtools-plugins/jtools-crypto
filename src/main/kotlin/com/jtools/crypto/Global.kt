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
import org.apache.commons.codec.digest.DigestUtils
import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.stream.Collectors
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
@State(name = "jtools-crypto", storages = [Storage(value = "JToolsCrypto.xml")])
class Global : PersistentStateComponent<GlobalState> {

    private var state: GlobalState = GlobalState()

    companion object {
//        fun getInstance(project: Project): GlobalState = project.getService(Global::class.java).state
        fun getInstance(project: Project): GlobalState = GlobalState()
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
                        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo7LwQlH3ErS3ejLS3cFv9KkLVYsRjLTx
                        Axf36d4qUBc4mw+CRi0HDJvcTYpY82tXo/U8tSJSEV9BGSshPUuj7ZVwBS3GvBo1gqcliME526eJ
                        m4Ss24AUp53HnahnBJUY1YpDXs6pbAyJfjITAAdmf3JZ7wPqgYuM1yGjCF6TYGZbmbybeJA18It0
                        K2WWCbRbq3Qy/Oy9cwisXB8sz9teXka41uJAVN5yv4tRpLoPlQDX/Pa9jmfj0bxunlpgvJMTcTxP
                        z5WI4wSCcotkolLP1KZUZtXb+4jHR7qhV7B/XmcmNPIWxZmaj9dQCbsMTXD5mYCRdD7+cdUjliRM
                        L73IqQIDAQAB
                      type: RSA_PUBLIC
                      name: 测试_RSA_PUBLIC
                      encryptType: base64
                    - key: |
                        MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjsvBCUfcStLd6MtLdwW/0qQtV
                        ixGMtPEDF/fp3ipQFzibD4JGLQcMm9xNiljza1ej9Ty1IlIRX0EZKyE9S6PtlXAFLca8GjWCpyWI
                        wTnbp4mbhKzbgBSnncedqGcElRjVikNezqlsDIl+MhMAB2Z/clnvA+qBi4zXIaMIXpNgZluZvJt4
                        kDXwi3QrZZYJtFurdDL87L1zCKxcHyzP215eRrjW4kBU3nK/i1Gkug+VANf89r2OZ+PRvG6eWmC8
                        kxNxPE/PlYjjBIJyi2SiUs/UplRm1dv7iMdHuqFXsH9eZyY08hbFmZqP11AJuwxNcPmZgJF0Pv5x
                        1SOWJEwvvcipAgMBAAECggEADL9MhweRszjUIE2IntjXNmC7B5dnn5LAAVcKmMEyf4/dWjLKGNud
                        BMn2zOl47YSme5enWW4RoLVvkO2LPjYPjvrP8TFMPF3YnKLWMQjl7+FDHZOGb+XQhfBrCM+CLSjM
                        mSVWA3IZnpa31rZAxUPuQv6lOu/twRZ8Okzt4IMX3B3ge7TjXHA3prERaXUtgu0B6HKRYFqK5AQ4
                        cSHVRR7e6tHjmEzY3Qbo9CYy3FJfNtLL1x0kPwNoPkkXbdwRj4yLNa6PK4HvACYvBGyF8412NtzF
                        SElFlH7YjKk+cBLc/sSwf8doWgg6/taLVXi9H8qKrLLHvowIgYgCD27V/kLIgQKBgQDEf65vt+uE
                        b2tbgkNWVGamt723n8Zq34fLQM88Z6EVuYLod38srd3QjLeWXDoJk4X7VhZ8jgQnX0b1w3Sv7WL9
                        NiygaM0hutNSirLux0QqrU/0q7c8a5T7Sf67+Qw56hVxf0tW4Yw9DiTN0MmWm1oJ51KUw5dZs7W5
                        PZu4HOFJKQKBgQDVRKmig/x3uKhAg4g+fI/mGfHFTTbqCkCQ+pee2DjG6BSmQfT0a3OiCe/Ke0Wm
                        n7FxhvrFow9kpwNFlBlEs9GZLQVfH/9wABV9Jq3FkSWEwEDgz0nlPnQy8zqb1XMtXQJdvCiNUS8H
                        iVuRWh20cqtLdPuyBMuh+wA9qPGMRlbzgQKBgGICDU9LvPzhVfrejheRIWImY0ojNyQ3OdP3D2Zh
                        icb8MH8imxv93JUYmrk0Zv4/xqQF3FFjxE6fBxWt15WoIaeKTdf0bdxVAvI4m+bdHPLsXuT6gPpx
                        7m0oCxZWsfJw/yENDNbYyMnrNBA0A1hLRC2MY3Qv2l2zdQBf6jt6rCOBAoGAfQ3nHomgWB9qYXH/
                        SQzHYFjkQRpW0gOy+pBGqfkFyfS6bwcL+OqtAl4rQqLoI51OclCA6bUEyLN6IqmF65g5lkUk+jOM
                        GAOklh9BFDFZO690G57RKOBnSJ0BR/FwuS/pvEchzEPHfPbZP7EE7BO1R+jP0gIn2dKRwJY5FVUF
                        lYECgYBTqE9msqOFEzlzXiAWxi+ACLUuesqopn1RPAIhTjm+W5TGou9WwU9QN6MM981cQVq60a7O
                        xEqaNkfHs0ETJZADUeFMilL6io4PQunQoX3NU3EOvFNeoKoq3vzhqZ69Ljw+EII21YGqhJViLxZd
                        mIQc9XfeBh7PXvDJFH+wi9ijDw==
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

enum class CryptoEnum(val decrypt: (Crypto, ByteArray) -> String, val encrypt: (Crypto, ByteArray) -> String) {
    UN_KNOW({ _, _ ->
        "不知道的解密类型,仅支持以下类型：${CryptoEnum.entries.filter { it.name != "UN_KNOW" }.map { it.name }.toList()}"
    }, { _, _ ->
        "不知道的加密类型,仅支持以下类型：${CryptoEnum.entries.filter { it.name != "UN_KNOW" }.map { it.name }.toList()}"
    }),

    MD5({ _, _ ->
        "md5不支持解密"
    }, { _, rawBytes ->
        DigestUtils.md5Hex(rawBytes)
    }),

    MD5_UPPER({_,_ -> "md5不支持解密"},{_,rawBytes -> DigestUtils.md5Hex(rawBytes).uppercase()}),


    DES_CBC({ c, rawBytes ->
        val cipher = Cipher.getInstance("DES/CBC/Pkcs5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(c.key?.toByteArray(StandardCharsets.UTF_8), "DES"),
            IvParameterSpec(c.iv?.toByteArray(StandardCharsets.UTF_8))
        )
        String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
    }, { c, rawBytes ->
        val cipher = Cipher.getInstance("DES/CBC/Pkcs5Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE, SecretKeySpec(
                c.key?.toByteArray(
                    StandardCharsets.UTF_8
                ), "DES"
            ),
            IvParameterSpec(c.iv?.toByteArray(StandardCharsets.UTF_8))
        )
        if (c.encryptType == "base64") {
            Base64.encodeBase64String(cipher.doFinal(rawBytes))
        } else {
            Hex.encodeHexString(cipher.doFinal(rawBytes))
        }
    }),

    DES_ECB({ c, rawBytes ->
        val cipher = Cipher.getInstance("DES/ECB/Pkcs5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(c.key?.toByteArray(StandardCharsets.UTF_8), "DES")
        )
        String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
    }, { c, rawBytes ->
        val cipher = Cipher.getInstance("DES/ECB/Pkcs5Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE, SecretKeySpec(
                c.key?.toByteArray(
                    StandardCharsets.UTF_8
                ), "DES"
            )
        )
        if (c.encryptType == "base64") {
            Base64.encodeBase64String(cipher.doFinal(rawBytes))
        } else {
            Hex.encodeHexString(cipher.doFinal(rawBytes))
        }
    }),

    AES_CBC({ c, rawBytes ->
        val cipher = Cipher.getInstance("AES/CBC/Pkcs5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(c.key?.toByteArray(StandardCharsets.UTF_8), "AES"),
            IvParameterSpec(c.iv?.toByteArray(StandardCharsets.UTF_8))
        )
        String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
    }, { c, rawBytes ->
        val cipher = Cipher.getInstance("AES/CBC/Pkcs5Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE, SecretKeySpec(
                c.key?.toByteArray(
                    StandardCharsets.UTF_8
                ), "AES"
            ),
            IvParameterSpec(c.iv?.toByteArray(StandardCharsets.UTF_8))
        )
        if (c.encryptType == "base64") {
            Base64.encodeBase64String(cipher.doFinal(rawBytes))
        } else {
            Hex.encodeHexString(cipher.doFinal(rawBytes))
        }
    }),

    AES_ECB({ c, rawBytes ->
        val cipher = Cipher.getInstance("AES/ECB/Pkcs5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(c.key?.toByteArray(StandardCharsets.UTF_8), "AES")
        )
        String(cipher.doFinal(rawBytes), StandardCharsets.UTF_8)
    }, { c, rawBytes ->
        val cipher = Cipher.getInstance("AES/ECB/Pkcs5Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE, SecretKeySpec(
                c.key?.toByteArray(
                    StandardCharsets.UTF_8
                ), "AES"
            )
        )
        if (c.encryptType == "base64") {
            Base64.encodeBase64String(cipher.doFinal(rawBytes))
        } else {
            Hex.encodeHexString(cipher.doFinal(rawBytes))
        }
    }),
    RSA_PUBLIC({ c, rawBytes ->
        c.key?.let {
            X509EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                KeyFactory.getInstance("RSA")?.generatePublic(key)
            }
        }?.let {
            Cipher.getInstance("RSA")?.let { rsa ->
                rsa.init(Cipher.DECRYPT_MODE, it)
                String(rsa.doFinal(rawBytes), StandardCharsets.UTF_8)
            }
        } ?: ""
    }, { c, rawBytes ->
        c.key?.let {
            X509EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                KeyFactory.getInstance("RSA")?.generatePublic(key)
            }
        }?.let {
            Cipher.getInstance("RSA")?.let { rsa ->
                rsa.init(Cipher.ENCRYPT_MODE, it)
                if (c.encryptType == "base64") {
                    Base64.encodeBase64String(rsa.doFinal(rawBytes))
                } else {
                    Hex.encodeHexString(rsa.doFinal(rawBytes))
                }
            }
        } ?: ""
    }),
    RSA_PRIVATE({ c, rawBytes ->
        c.key?.let {
            PKCS8EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                KeyFactory.getInstance("RSA")?.generatePrivate(key)
            }
        }?.let {
            Cipher.getInstance("RSA")?.let { rsa ->
                rsa.init(Cipher.DECRYPT_MODE, it)
                String(rsa.doFinal(rawBytes), StandardCharsets.UTF_8)
            }
        } ?: ""
    }, { c, rawBytes ->
        c.key?.let {
            PKCS8EncodedKeySpec(Base64.decodeBase64(it)).let { key ->
                KeyFactory.getInstance("RSA")?.generatePrivate(key)
            }
        }?.let {
            Cipher.getInstance("RSA")?.let { rsa ->
                rsa.init(Cipher.ENCRYPT_MODE, it)
                if (c.encryptType == "base64") {
                    Base64.encodeBase64String(rsa.doFinal(rawBytes))
                } else {
                    Hex.encodeHexString(rsa.doFinal(rawBytes))
                }
            }
        } ?: ""
    });

    companion object {
        private val cache: MutableMap<String, CryptoEnum> =
            CryptoEnum.entries.stream().collect(Collectors.toMap({ it.name }, { it }))

        fun find(name: String): CryptoEnum {
            return cache.getOrDefault(name, CryptoEnum.UN_KNOW)
        }
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
        return CryptoEnum.find(this.type ?: "").decrypt.invoke(this, rawBytes)
    }

    fun encrypt(rawBytes: ByteArray): String {
        return try {
            CryptoEnum.find(this.type ?: "").encrypt.invoke(this, rawBytes)
        } catch (e: Throwable) {
            "加密失败: \r\n$e\r\n${e.stackTrace.map { it.toString() }}"
        }

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