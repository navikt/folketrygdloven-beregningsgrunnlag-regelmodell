package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
public class FastsettForFrilans extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Fastsetter beregnignsgrunnlag for frilans";

    public FastsettForFrilans() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();

        BigDecimal totalTilFastsetting = grunnlag.getGrenseverdi();

        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        Optional<BeregningsgrunnlagPrArbeidsforhold> frilansArbeidsforhold = atflAndel == null ? Optional.empty() : atflAndel
            .getFrilansArbeidsforhold();

        if (frilansArbeidsforhold.isPresent()) {
            BigDecimal bortfaltFL = frilansArbeidsforhold
                .flatMap(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoInkludertNaturalytelsePrÅr)
                .orElse(BigDecimal.ZERO);
            BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold = frilansArbeidsforhold.get();
            if (bortfaltFL.compareTo(totalTilFastsetting) > 0) {
                BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
                    .medAvkortetPrÅr(totalTilFastsetting)
                    .medAvkortetBrukersAndelPrÅr(totalTilFastsetting)
                    .medAvkortetRefusjonPrÅr(BigDecimal.ZERO)
                    .medMaksimalRefusjonPrÅr(BigDecimal.ZERO)
                    .build();
            } else {
                BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
                    .medAvkortetPrÅr(bortfaltFL)
                    .medAvkortetBrukersAndelPrÅr(bortfaltFL)
                    .medAvkortetRefusjonPrÅr(BigDecimal.ZERO)
                    .medMaksimalRefusjonPrÅr(BigDecimal.ZERO)
                    .build();
            }
            resultater.put("avkortetFrilans", beregningsgrunnlagPrArbeidsforhold.getAvkortetPrÅr());
        }
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
