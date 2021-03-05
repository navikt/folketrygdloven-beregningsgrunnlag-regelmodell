package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelEndring;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellRefusjonOgNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;

class ErbruttoinntektForGradertAndelLikNull {

    private ErbruttoinntektForGradertAndelLikNull(){
    }

    public static boolean vurder(PeriodeModell input, AndelEndring andelGradering, LocalDate dato){
        for(PeriodisertBruttoBeregningsgrunnlag periodisertGrunnlag : input.getPeriodisertBruttoBeregningsgrunnlagList()){
            if(periodisertGrunnlag.getPeriode().inneholder(dato)){
                Optional<BruttoBeregningsgrunnlag> grunnlag = finnBruttoBeregningsgrunnlagForGradering(periodisertGrunnlag.getBruttoBeregningsgrunnlag(), andelGradering);
                if(grunnlag.isPresent()){
                    return grunnlag.get().getBruttoPrÅr().equals(BigDecimal.ZERO);
                } else{
                    throw new IllegalStateException("Finner ikke matchende bruttoberegningsgrunnlag for andel som skal graderes " + andelGradering);
                }
            }
        }
        return false;
    }

    private static Optional<BruttoBeregningsgrunnlag> finnBruttoBeregningsgrunnlagForGradering(List<BruttoBeregningsgrunnlag> beregningsgrunnlag, AndelEndring andelGradering){
        if(andelErSnEllerFl(andelGradering)){
            return beregningsgrunnlag.stream()
                .filter(b -> b.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()))
                .findAny();
        }

        return  finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(beregningsgrunnlag, andelGradering.getArbeidsforhold());
    }

    private static Optional<BruttoBeregningsgrunnlag> finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(List<BruttoBeregningsgrunnlag> beregningsgrunnlag, Arbeidsforhold arbeidsforhold){
        return beregningsgrunnlag.stream()
            .filter(b -> b.getArbeidsforhold().map(a -> a.getArbeidsgiverId().equals(arbeidsforhold.getArbeidsgiverId())
		            && (a.getArbeidsforholdId() == null
		            || arbeidsforhold.getArbeidsforholdId() == null
		            || a.getArbeidsforholdId().equals(arbeidsforhold.getArbeidsforholdId()))).orElse(false))
            .findFirst();
    }

    private static boolean andelErSnEllerFl(AndelEndring andelGradering) {
        return AktivitetStatusV2.FL.equals(andelGradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(andelGradering.getAktivitetStatus());
    }
}
