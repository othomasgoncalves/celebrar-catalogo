package com.thomas.celebrarcatalog.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TentativasLoginServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-29T12:00:00Z");

    @Mock
    private UsuarioRepository usuarioRepository;

    private TentativasLoginService servico(Instant instante) {
        return new TentativasLoginService(usuarioRepository, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void conta_nova_nao_esta_bloqueada() {
        assertThat(servico(AGORA).estaBloqueado(UsuarioFixture.admin())).isFalse();
    }

    @Test
    void as_quatro_primeiras_falhas_nao_bloqueiam() {
        TentativasLoginService servico = servico(AGORA);
        Usuario usuario = UsuarioFixture.admin();

        for (int tentativa = 1; tentativa <= 4; tentativa++) {
            servico.registrarFalha(usuario);
            assertThat(usuario.getFalhasLogin()).isEqualTo(tentativa);
            assertThat(usuario.getBloqueadoAte()).isNull();
            assertThat(servico.estaBloqueado(usuario)).isFalse();
        }
    }

    @Test
    void a_quinta_falha_bloqueia_por_quinze_minutos() {
        TentativasLoginService servico = servico(AGORA);
        Usuario usuario = UsuarioFixture.comFalhas(4);

        servico.registrarFalha(usuario);

        assertThat(usuario.getFalhasLogin()).isEqualTo(5);
        assertThat(usuario.getBloqueadoAte()).isEqualTo(AGORA.plus(Duration.ofMinutes(15)));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void permanece_bloqueado_durante_a_janela_inteira() {
        Usuario usuario = UsuarioFixture.bloqueadoAte(AGORA.plus(Duration.ofMinutes(15)));

        assertThat(servico(AGORA).estaBloqueado(usuario)).isTrue();
        assertThat(servico(AGORA.plus(Duration.ofMinutes(14))).estaBloqueado(usuario)).isTrue();
        // O instante exato do vencimento ja libera (bloqueadoAte nao esta mais no futuro).
        assertThat(servico(AGORA.plus(Duration.ofMinutes(15))).estaBloqueado(usuario)).isFalse();
    }

    @Test
    void bloqueio_vencido_libera_e_zera_o_contador_persistindo_a_mudanca() {
        Usuario usuario = UsuarioFixture.bloqueadoAte(AGORA);

        boolean bloqueado = servico(AGORA.plusSeconds(1)).estaBloqueado(usuario);

        assertThat(bloqueado).isFalse();
        assertThat(usuario.getFalhasLogin()).isZero();
        assertThat(usuario.getBloqueadoAte()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void sucesso_zera_falhas_e_bloqueio() {
        Usuario usuario = UsuarioFixture.bloqueadoAte(AGORA.plus(Duration.ofMinutes(15)));

        servico(AGORA).registrarSucesso(usuario);

        assertThat(usuario.getFalhasLogin()).isZero();
        assertThat(usuario.getBloqueadoAte()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void sucesso_em_conta_limpa_nao_gera_escrita_desnecessaria() {
        servico(AGORA).registrarSucesso(UsuarioFixture.admin());

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void depois_de_liberado_a_contagem_recomeca_do_zero() {
        Usuario usuario = UsuarioFixture.bloqueadoAte(AGORA);
        Instant depois = AGORA.plusSeconds(1);
        TentativasLoginService servico = servico(depois);

        servico.estaBloqueado(usuario); // libera e zera
        servico.registrarFalha(usuario);

        assertThat(usuario.getFalhasLogin()).isEqualTo(1);
        assertThat(usuario.getBloqueadoAte()).isNull();
    }
}
