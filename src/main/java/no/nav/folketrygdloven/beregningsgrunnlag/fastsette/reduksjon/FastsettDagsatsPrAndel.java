package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.reduksjon;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettDagsatsPrAndel.ID)
public class FastsettDagsatsPrAndel extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 6.1";
    static final String BESKRIVELSE = "Fastsett dagsats per beregningsgrunnlagandel";

    public FastsettDagsatsPrAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes().forEach(bgps -> {
            if (bgps.erArbeidstakerEllerFrilanser()) {
                bgps.getArbeidsforholdSomSkalBrukes().forEach(af ->
                    resultater.put("dagsats." + af.getArbeidsgiverId(), af.getDagsats())
                );
            } else {
                Long dagsats = bgps.getRedusertPrÅr().divide(grunnlag.getYtelsedagerPrÅr(), 0, RoundingMode.HALF_UP).longValue();
                resultater.put("dagsats." + bgps.getAktivitetStatus().name(), dagsats);
            }

        });
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
