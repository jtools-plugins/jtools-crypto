package com.jtools.crypto

import com.lhstack.tools.plugins.Logger
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.util.*
import java.util.function.Consumer

val generators = sortedMapOf<String, Consumer<Logger>>().apply {
    this.put("AES_ECB") {
        it.activeConsolePanel().info("\nAES_ECB: ${RandomStringUtils.randomAlphanumeric(16)}")
    }
    this.put("AES_CBC") {
        it.activeConsolePanel().info(
            """
                
            AES_CBC: 
                KEY: ${RandomStringUtils.randomAlphanumeric(16)}
                IV: ${RandomStringUtils.randomAlphanumeric(16)}
        """.trimIndent()
        )
    }

    this.put("AES_ECB_BASE64") {
        it.activeConsolePanel().info("\nAES_ECB: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(16))}")
    }

    this.put("AES_CBC_BASE64") {
        it.activeConsolePanel().info(
            """
                
            AES_CBC: 
                KEY: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(16))}
                IV: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(16))}
        """.trimIndent()
        )
    }

    this.put("AES_ECB_HEX") {
        it.activeConsolePanel().info("\nAES_ECB: ${Hex.encodeHexString(RandomUtils.nextBytes(16))}")
    }

    this.put("AES_CBC_HEX") {
        it.activeConsolePanel().info(
            """
                
            AES_CBC: 
                KEY: ${Hex.encodeHexString(RandomUtils.nextBytes(16))}
                IV: ${Hex.encodeHexString(RandomUtils.nextBytes(16))}
        """.trimIndent()
        )
    }


    this.put("DES_ECB") {
        it.activeConsolePanel().info("\nAES_ECB: ${RandomStringUtils.randomAlphanumeric(8)}")
    }
    this.put("DES_CBC") {
        it.activeConsolePanel().info(
            """
                
            DES_CBC: 
                KEY: ${RandomStringUtils.randomAlphanumeric(8)}
                IV: ${RandomStringUtils.randomAlphanumeric(8)}
        """.trimIndent()
        )
    }

    this.put("DES_ECB_BASE64") {
        it.activeConsolePanel().info("\nDES_ECB: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(8))}")
    }

    this.put("DES_CBC_BASE64") {
        it.activeConsolePanel().info(
            """
                
            DES_CBC: 
                KEY: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(8))}
                IV: ${Base64.getEncoder().encodeToString(RandomUtils.nextBytes(8))}
        """.trimIndent()
        )
    }

    this.put("DES_ECB_HEX") {
        it.activeConsolePanel().info("\nDES_ECB: ${Hex.encodeHexString(RandomUtils.nextBytes(8))}")
    }

    this.put("DES_CBC_HEX") {
        it.activeConsolePanel().info(
            """
                
            DES_CBC: 
                KEY: ${Hex.encodeHexString(RandomUtils.nextBytes(8))}
                IV: ${Hex.encodeHexString(RandomUtils.nextBytes(8))}
        """.trimIndent()
        )
    }


    this.put("RSA_1024"){
        val rsa = KeyPairGenerator.getInstance("RSA")
        rsa.initialize(1024)
        val generateKeyPair = rsa.generateKeyPair()
        it.activeConsolePanel().info(
            """ 
                
            公钥: ${Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)}
            私钥: ${Base64.getEncoder().encodeToString(generateKeyPair.private.encoded)}
        """.trimIndent()
        )
    }

    this.put("RSA_2048"){
        val rsa = KeyPairGenerator.getInstance("RSA")
        rsa.initialize(2048)
        val generateKeyPair = rsa.generateKeyPair()
        it.activeConsolePanel().info(
            """ 
                
            公钥: ${Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)}
            私钥: ${Base64.getEncoder().encodeToString(generateKeyPair.private.encoded)}
        """.trimIndent()
        )
    }


    this.put("ECDSA_256"){
        val rsa = KeyPairGenerator.getInstance("EC")
        rsa.initialize(256)
        val generateKeyPair = rsa.generateKeyPair()
        it.activeConsolePanel().info(
            """ 
                
            公钥: ${Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)}
            私钥: ${Base64.getEncoder().encodeToString(generateKeyPair.private.encoded)}
        """.trimIndent()
        )
    }

    this.put("ECDSA_384"){
        val rsa = KeyPairGenerator.getInstance("EC")
        rsa.initialize(384)
        val generateKeyPair = rsa.generateKeyPair()
        it.activeConsolePanel().info(
            """ 
                
            公钥: ${Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)}
            私钥: ${Base64.getEncoder().encodeToString(generateKeyPair.private.encoded)}
        """.trimIndent()
        )
    }

    this.put("ECDSA_521"){
        val rsa = KeyPairGenerator.getInstance("EC")
        rsa.initialize(521)
        val generateKeyPair = rsa.generateKeyPair()
        it.activeConsolePanel().info(
            """ 
                
            公钥: ${Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)}
            私钥: ${Base64.getEncoder().encodeToString(generateKeyPair.private.encoded)}
        """.trimIndent()
        )
    }

}


