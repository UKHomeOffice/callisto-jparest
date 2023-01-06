package uk.gov.homeoffice.digital.sas.mutualtls.kafka;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import lombok.Getter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class GenerateCsr {

  @Getter
  public static final PublicKey publicKey = null;
  @Getter
  public static final PrivateKey privateKey = null;
  @Getter
  public static final GenerateCsr gcsr = null;

  public static KeyPair generateRsaKeyPair() throws GeneralSecurityException {
    KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");

    kpGen.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));

    return kpGen.generateKeyPair();
  }

  public PKCS10CertificationRequest createPkcs10(KeyPair keyPair, String sigAlg)
      throws OperatorCreationException {
    X500Name subject = new X500Name("CN=TimeCard");

    PKCS10CertificationRequestBuilder requestBuilder = new JcaPKCS10CertificationRequestBuilder(
        subject, keyPair.getPublic());
    ContentSigner signer =
        new JcaContentSignerBuilder(sigAlg).build(keyPair.getPrivate());

    return requestBuilder.build(signer);
  }
}
