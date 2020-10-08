package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmManueltFastsattBeregningsgrunnlagSN.ID)
public class SjekkOmManueltFastsattBeregningsgrunnlagSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.19";
    static final String BESKRIVELSE = "Har saksbehandler fastsatt beregningsgrunnlaget manuelt?";

    public SjekkOmManueltFastsattBeregningsgrunnlagSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).erFastsattAvSaksbehandler())
            ? ja()
            : nei();
    }
}
