package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.kunYtelse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagTY.ID)
class ForeslåBeregningsgrunnlagTY extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for status tilstøtende ytelse";
    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.F_14_7;

    ForeslåBeregningsgrunnlagTY() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.KUN_YTELSE).setHjemmel(HJEMMEL);
        BigDecimal brutto = grunnlag.getBruttoPrÅr();
        resultater.put("beregnetPrÅr", brutto);
        resultater.put("hjemmel", HJEMMEL);
        return beregnet(resultater);
    }
}
