package com.thomas.celebrarcatalog.auth;

import com.thomas.celebrarcatalog.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtService {

    private static final String ALGORITMO_MAC = "HmacSHA256";
    private static final String ALG_ESPERADO = "HS256";
    private static final String TYP_ESPERADO = "JWT";
    private static final String PREFIXO_ROLE = "ROLE_";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecretKeySpec chave;
    private final Duration expiracao;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    JwtService(JwtProperties propriedades, ObjectMapper objectMapper) {
        this(propriedades, objectMapper, Clock.systemUTC());
    }

    JwtService(JwtProperties propriedades, ObjectMapper objectMapper, Clock clock) {
        this.chave = propriedades.chaveHmac();
        this.expiracao = propriedades.expiracao();
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String gerarToken(Usuario usuario) {
        long agora = clock.instant().getEpochSecond();

        ObjectNode cabecalho = objectMapper.createObjectNode();
        cabecalho.put("alg", ALG_ESPERADO);
        cabecalho.put("typ", TYP_ESPERADO);

        ObjectNode corpo = objectMapper.createObjectNode();
        corpo.put("sub", usuario.getId().toString());
        corpo.put("email", usuario.getEmail());
        corpo.put("role", usuario.getRole());
        corpo.put("iat", agora);
        corpo.put("exp", agora + expiracao.getSeconds());
        corpo.put("jti", UUID.randomUUID().toString());

        String conteudo = codificar(serializar(cabecalho)) + "." + codificar(serializar(corpo));
        return conteudo + "." + codificar(assinar(conteudo));
    }

    public UsuarioAutenticado validar(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtInvalidoException("token ausente");
        }

        String[] partes = token.split("\\.", -1);
        if (partes.length != 3) {
            throw new JwtInvalidoException("token nao tem as tres partes de um JWS compacto");
        }

        JsonNode cabecalho = desserializar(decodificar(partes[0], "cabecalho"), "cabecalho");
        String alg = texto(cabecalho, "alg");
        if (alg == null) {
            throw new JwtInvalidoException("cabecalho sem 'alg'");
        }
        if ("none".equalsIgnoreCase(alg)) {
            throw new JwtInvalidoException("token sem assinatura (alg 'none') recusado");
        }
        if (!ALG_ESPERADO.equals(alg)) {
            throw new JwtInvalidoException("algoritmo nao suportado: " + alg);
        }
        String typ = texto(cabecalho, "typ");
        if (typ != null && !TYP_ESPERADO.equalsIgnoreCase(typ)) {
            throw new JwtInvalidoException("typ nao suportado: " + typ);
        }

        byte[] assinaturaRecebida = decodificar(partes[2], "assinatura");
        byte[] assinaturaEsperada = assinar(partes[0] + "." + partes[1]);
        if (!MessageDigest.isEqual(assinaturaEsperada, assinaturaRecebida)) {
            throw new JwtInvalidoException("assinatura invalida");
        }

        JsonNode corpo = desserializar(decodificar(partes[1], "corpo"), "corpo");

        long agora = clock.instant().getEpochSecond();
        long exp = numero(corpo, "exp");
        if (exp <= agora) {
            throw new JwtInvalidoException("token expirado");
        }
        numero(corpo, "iat");

        if (texto(corpo, "jti") == null) {
            throw new JwtInvalidoException("claim 'jti' ausente");
        }

        String sub = texto(corpo, "sub");
        if (sub == null) {
            throw new JwtInvalidoException("claim 'sub' ausente");
        }
        UUID id;
        try {
            id = UUID.fromString(sub);
        } catch (IllegalArgumentException excecao) {
            throw new JwtInvalidoException("claim 'sub' nao e um UUID", excecao);
        }

        String email = texto(corpo, "email");
        if (email == null) {
            throw new JwtInvalidoException("claim 'email' ausente");
        }

        String role = texto(corpo, "role");
        if (role == null || !role.startsWith(PREFIXO_ROLE)) {
            throw new JwtInvalidoException("claim 'role' ausente ou sem o prefixo ROLE_");
        }

        return new UsuarioAutenticado(id, email, role);
    }

    private byte[] assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_MAC);
            mac.init(chave);
            return mac.doFinal(conteudo.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException excecao) {
            throw new IllegalStateException("Falha ao calcular o HMAC do token", excecao);
        }
    }

    private byte[] serializar(JsonNode no) {
        try {
            return objectMapper.writeValueAsBytes(no);
        } catch (JacksonException excecao) {
            throw new IllegalStateException("Falha ao serializar o token", excecao);
        }
    }

    private JsonNode desserializar(byte[] json, String parte) {
        JsonNode no;
        try {
            no = objectMapper.readTree(json);
        } catch (JacksonException excecao) {
            throw new JwtInvalidoException(parte + " do token nao e JSON valido", excecao);
        }
        if (no == null || !no.isObject()) {
            throw new JwtInvalidoException(parte + " do token nao e um objeto JSON");
        }
        return no;
    }

    private static String codificar(byte[] bytes) {
        return ENCODER.encodeToString(bytes);
    }

    private static byte[] decodificar(String parte, String nome) {
        try {
            return DECODER.decode(parte);
        } catch (IllegalArgumentException excecao) {
            throw new JwtInvalidoException(nome + " do token nao e base64url valido", excecao);
        }
    }

    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        if (valor == null || !valor.isString() || valor.asString().isBlank()) {
            return null;
        }
        return valor.asString();
    }

    private static long numero(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        if (valor == null || !valor.isNumber()) {
            throw new JwtInvalidoException("claim '" + campo + "' ausente ou nao numerica");
        }
        return valor.asLong();
    }
}
