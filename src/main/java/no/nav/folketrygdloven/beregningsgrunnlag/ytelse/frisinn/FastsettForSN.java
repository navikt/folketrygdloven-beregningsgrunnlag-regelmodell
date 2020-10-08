package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ReduserBeregningsgrunnlag.ID)
public class FastsettForSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Fastsetter beregnignsgrunnlag for frilans";

    public FastsettForSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();


        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal fastsattTilFrilans = atflAndel == null ? BigDecimal.ZERO : atflAndel
            .getFrilansArbeidsforhold()
            .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetPrÅr)
            .orElse(BigDecimal.ZERO);

        BigDecimal totalTilFastsetting = grunnlag.getGrenseverdi().subtract(fastsattTilFrilans).max(BigDecimal.ZERO);

        BeregningsgrunnlagPrStatus snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);

        if (snStatus != null) {
            BigDecimal bortfaltSN = snStatus.getGradertBruttoInkludertNaturalytelsePrÅr();
            if (bortfaltSN.compareTo(totalTilFastsetting) >= 0) {
                BeregningsgrunnlagPrStatus.builder(snStatus)
                    .medAvkortetPrÅr(totalTilFastsetting)
                    .build();
            } else {
                throw new IllegalStateException("Skal ikke ha igjen grunnlag etter fastsetting");
            }
            resultater.put("avkortetSN", snStatus.getAvkortetPrÅr());
        }
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
