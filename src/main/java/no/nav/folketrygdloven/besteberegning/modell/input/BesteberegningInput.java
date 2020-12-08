package no.nav.folketrygdloven.besteberegning.modell.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class BesteberegningInput {

	private final Inntektsgrunnlag inntektsgrunnlag;
	private final List<Grunnbeløp> grunnbeløpSatser;
	private final BigDecimal gjeldendeGverdi;
	private final LocalDate skjæringstidspunktOpptjening;
	private final List<Periode> perioderMedNæringsvirksomhet;
	/**
	 * Totalt brutto grunnlag beregnet etter kap 8 i folketrygdloven
	 */
	private final BigDecimal beregnetGrunnlag;

	public BesteberegningInput(Inntektsgrunnlag inntektsgrunnlag,
	                           List<Grunnbeløp> grunnbeløpSatser,
	                           BigDecimal gjeldendeGverdi,
	                           LocalDate skjæringstidspunktOpptjening,
	                           List<Periode> perioderMedNæringsvirksomhet,
	                           BigDecimal beregnetGrunnlag) {
		this.inntektsgrunnlag = inntektsgrunnlag;
		this.grunnbeløpSatser = grunnbeløpSatser;
		this.gjeldendeGverdi = gjeldendeGverdi;
		this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
		this.perioderMedNæringsvirksomhet = perioderMedNæringsvirksomhet;
		this.beregnetGrunnlag = beregnetGrunnlag;
	}

	public Inntektsgrunnlag getInntektsgrunnlag() {
		return inntektsgrunnlag;
	}

	public LocalDate getSkjæringstidspunktOpptjening() {
		return skjæringstidspunktOpptjening;
	}

	public List<Grunnbeløp> getGrunnbeløpSatser() {
		return grunnbeløpSatser;
	}

	public BigDecimal getGjeldendeGverdi() {
		return gjeldendeGverdi;
	}

	public List<Periode> getPerioderMedNæringsvirksomhet() {
		return perioderMedNæringsvirksomhet;
	}

	public BigDecimal getBeregnetGrunnlag() {
		return beregnetGrunnlag;
	}
}
