package com.thomas.celebrarcatalog.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class TentativasLoginService {

    static final int MAX_FALHAS = 5;
    static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @Autowired
    TentativasLoginService(UsuarioRepository usuarioRepository) {
        this(usuarioRepository, Clock.systemUTC());
    }

    TentativasLoginService(UsuarioRepository usuarioRepository, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Transactional
    public boolean estaBloqueado(Usuario usuario) {
        Instant bloqueadoAte = usuario.getBloqueadoAte();
        if (bloqueadoAte == null) {
            return false;
        }
        if (bloqueadoAte.isAfter(clock.instant())) {
            return true;
        }
        zerar(usuario);
        return false;
    }

    @Transactional
    public void registrarFalha(Usuario usuario) {
        int falhas = usuario.getFalhasLogin() + 1;
        usuario.definirFalhasLogin(falhas);
        if (falhas >= MAX_FALHAS) {
            usuario.definirBloqueadoAte(clock.instant().plus(DURACAO_BLOQUEIO));
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarSucesso(Usuario usuario) {
        if (usuario.getFalhasLogin() != 0 || usuario.getBloqueadoAte() != null) {
            zerar(usuario);
        }
    }

    private void zerar(Usuario usuario) {
        usuario.definirFalhasLogin(0);
        usuario.definirBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }
}
