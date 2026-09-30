package com.thomas.celebrarcatalog.auth;

import com.thomas.celebrarcatalog.comum.ErroResposta;
import com.thomas.celebrarcatalog.config.SessaoCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";

    private static final String CONTA_BLOQUEADA =
            "Muitas tentativas de login. Tente novamente em alguns minutos.";

    private static final String HASH_INEXISTENTE =
            "$2a$12$pTxhJO4ZjuTWQWpDouMFt.fWO3Rfpue2HuJj/Fpl5p6YEpow22hDW";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TentativasLoginService tentativasLogin;
    private final JwtService jwtService;
    private final SessaoCookie sessaoCookie;

    AuthController(UsuarioRepository usuarioRepository,
                   PasswordEncoder passwordEncoder,
                   TentativasLoginService tentativasLogin,
                   JwtService jwtService,
                   SessaoCookie sessaoCookie) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tentativasLogin = tentativasLogin;
        this.jwtService = jwtService;
        this.sessaoCookie = sessaoCookie;
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest requisicao, HttpServletResponse resposta) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(requisicao.email());

        if (encontrado.isEmpty()) {
            passwordEncoder.matches(requisicao.senha(), HASH_INEXISTENTE);
            return credenciaisInvalidas();
        }

        Usuario usuario = encontrado.get();

        if (tentativasLogin.estaBloqueado(usuario)) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(ErroResposta.de(CONTA_BLOQUEADA));
        }

        if (!usuario.isAtivo()) {
            return credenciaisInvalidas();
        }

        if (!passwordEncoder.matches(requisicao.senha(), usuario.getSenhaHash())) {
            tentativasLogin.registrarFalha(usuario);
            return credenciaisInvalidas();
        }

        tentativasLogin.registrarSucesso(usuario);
        sessaoCookie.gravar(resposta, jwtService.gerarToken(usuario));
        return ResponseEntity.ok(UsuarioDto.from(usuario));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletResponse resposta) {
        sessaoCookie.limpar(resposta);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eu")
    ResponseEntity<UsuarioDto> eu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(UsuarioDto.from(usuario));
    }

    private static ResponseEntity<ErroResposta> credenciaisInvalidas() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResposta.de(CREDENCIAIS_INVALIDAS));
    }
}
