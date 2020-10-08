package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerErNyIArbeidslivet.ID)
public class SjekkOmBrukerErNyIArbeidslivet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.18";
    static final String BESKRIVELSE = "Er bruker SN som er ny i arbeidslivet?";

    public static final RuleReasonRef FASTSETT_BG_FOR_SN_NY_I_ARBEIDSLIVET = new RuleReasonRefImpl("5049",
        "Fastsett beregningsgrunnlag for selvstendig næringsdrivende som er ny i arbeidslivet");

    public SjekkOmBrukerErNyIArbeidslivet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getNyIArbeidslivet())
            ? ja()
            : nei();
    }
}
