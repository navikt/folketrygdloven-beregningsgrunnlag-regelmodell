package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBeregninsgrunnlagErBesteberegnet.ID)
public class SjekkOmBeregninsgrunnlagErBesteberegnet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    /**
     * https://jira.adeo.no/browse/TFP-2806
     * Når beregningsgrunnlaget er fastsatt etter besteberegning skal det ikke avviksvurderes
     */
    static final String ID = "FP_BR 2.20";
    static final String BESKRIVELSE = "Er beregningsgrunnlaget besteberegnet?";

    public SjekkOmBeregninsgrunnlagErBesteberegnet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(grunnlag.erBesteberegnet())
            ? ja()
            : nei();
    }
}
