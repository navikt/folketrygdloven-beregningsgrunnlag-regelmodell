package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAndelForArbeidsforhold.ID)
class FastsettAndelForArbeidsforhold extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.6";
    static final String BESKRIVELSE = "Fastsett andeleler pr Arbeidsforhold";

    FastsettAndelForArbeidsforhold() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SingleEvaluation resultat = ja();
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdene = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforholdIkkeFrilans();
        Map<String, Object> resultater = new HashMap<>();
        resultat.setEvaluationProperties(resultater);
        BigDecimal grenseverdi = grunnlag.getGrenseverdi();
        resultater.put("grenseverdi", grenseverdi);
        fastsettAndelerPrArbeidsforhold(arbeidsforholdene, resultater, grenseverdi);
        return resultat ;
    }

    private void fastsettAndelerPrArbeidsforhold(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdList, Map<String, Object> resultater,
                                                 BigDecimal ikkeFordelt) {
        BigDecimal sumBruttoBG = arbeidsforholdList.stream()
                .map(af -> af.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        arbeidsforholdList.forEach(af -> {
            BigDecimal prosentandel = BigDecimal.valueOf(100)
                .multiply(af.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .divide(sumBruttoBG, 10, RoundingMode.HALF_EVEN);
            resultater.put("gjenstårÅFastsetteRefusjon.prosentandel." + af.getArbeidsgiverId(), prosentandel);
            BigDecimal andel = ikkeFordelt.multiply(af.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .divide(sumBruttoBG, 10, RoundingMode.HALF_EVEN);
            BeregningsgrunnlagPrArbeidsforhold.builder(af)
                .medAndelsmessigFørGraderingPrAar(andel)
                .build();
            resultater.put("brukersAndel." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
        });
    }
}
