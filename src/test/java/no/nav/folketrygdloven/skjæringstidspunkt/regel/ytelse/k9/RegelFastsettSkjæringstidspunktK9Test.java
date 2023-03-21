package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.k9;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegelFastsettSkjæringstidspunktK9Test {

	@Test
	void skal_sette_stp_lik_stp_for_opptjening() {
		var modell = new AktivitetStatusModell();
		var stpOpptjening = LocalDate.of(2023, 6, 6);
		modell.setSkjæringstidspunktForOpptjening(stpOpptjening);

		new RegelFastsettSkjæringstidspunktK9().evaluer(modell);

		assertThat(modell.getSkjæringstidspunktForBeregning()).isNotNull();
		assertThat(modell.getSkjæringstidspunktForBeregning()).isEqualTo(stpOpptjening);
	}
}