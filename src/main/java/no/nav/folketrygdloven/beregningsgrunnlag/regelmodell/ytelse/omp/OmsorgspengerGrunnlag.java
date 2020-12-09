package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private BigDecimal gradertRefusjonVedSkjæringstidspunkt;

    public OmsorgspengerGrunnlag(BigDecimal gradertRefusjonVedSkjæringstidspunkt) {
        super("OMP");
        this.gradertRefusjonVedSkjæringstidspunkt = gradertRefusjonVedSkjæringstidspunkt;
    }

    public BigDecimal getGradertRefusjonVedSkjæringstidspunkt() {
        return gradertRefusjonVedSkjæringstidspunkt;
    }

    public boolean erDirekteUtbetaling() {
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal minsteRefusjon = førstePeriode.getGrenseverdi().min(this.getGradertRefusjonVedSkjæringstidspunkt());
        BigDecimal totaltBeregningsgrunnlag = førstePeriode.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = førstePeriode.getGrenseverdi().min(totaltBeregningsgrunnlag);
        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }


    @Override
    public BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        if (!arbeidsforhold.erFrilanser() && arbeidsforhold.getArbeidsgiverId() != null && periode.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() != null) {
            return fordelRestinntektFraAOrdningen(beregnetPrÅr, arbeidsforhold, periode);
        }
        return beregnetPrÅr;
    }

    private BigDecimal fordelRestinntektFraAOrdningen(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        Inntektsgrunnlag inntektsgrunnlag = periode.getInntektsgrunnlag();
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdISammeOrg = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforholdIkkeFrilans()
            .stream()
            .filter(a -> a.getArbeidsgiverId() != null)
            .filter(a -> a.getArbeidsgiverId().equals(arbeidsforhold.getArbeidsgiverId()))
            .collect(Collectors.toList());
        BigDecimal beløpFraInntektsmeldingPrÅr = arbeidsforholdISammeOrg.stream()
            .map(inntektsgrunnlag::getInntektFraInntektsmelding)
            .map(beløp -> beløp.multiply(BigDecimal.valueOf(12)))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        if (beløpFraInntektsmeldingPrÅr.compareTo(beregnetPrÅr) > 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal restFraAOrdningen = beregnetPrÅr.subtract(beløpFraInntektsmeldingPrÅr);

        long antallArbeidsforholdUtenInntektsmelding = arbeidsforholdISammeOrg.stream()
            .filter(a -> inntektsgrunnlag.getInntektFraInntektsmelding(a) == null || inntektsgrunnlag.getInntektFraInntektsmelding(a).compareTo(BigDecimal.ZERO) == 0)
            .count();

        if (antallArbeidsforholdUtenInntektsmelding == 0) {
            throw new IllegalStateException("Kan ikke beregne andel fra aordningen når alle andeler for arbeidsgiver med orgnr " + arbeidsforhold.getArbeidsgiverId() +
                " har inntektsmelding");
        }

        return restFraAOrdningen.divide(BigDecimal.valueOf(antallArbeidsforholdUtenInntektsmelding), RoundingMode.HALF_EVEN);
    }
}
