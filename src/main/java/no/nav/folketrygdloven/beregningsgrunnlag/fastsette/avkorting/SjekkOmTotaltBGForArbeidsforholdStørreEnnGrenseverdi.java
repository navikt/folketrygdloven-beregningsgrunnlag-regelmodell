package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.avkorting;

import java.math.BigDecimal;


import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdi.ID)
class SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdi extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.8.2";
    static final String BESKRIVELSE = "Er totalt beregningsgrunnlag for beregningsgrunnlagsandeler fra arbeidsforhold større enn 6G/grenseverdi";

    SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdi() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal grenseverdi = grunnlag.getGrenseverdi();
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal totaltBG = atfl == null ? BigDecimal.ZERO : atfl.getArbeidsforholdSomSkalBrukesIkkeFrilans().stream()
            .map(af -> af.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        SingleEvaluation resultat = totaltBG.compareTo(grenseverdi) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totaltBeregningsgrunnlagFraArbeidsforhold", totaltBG);
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("grenseverdi", grenseverdi);
        return resultat;
    }
}
