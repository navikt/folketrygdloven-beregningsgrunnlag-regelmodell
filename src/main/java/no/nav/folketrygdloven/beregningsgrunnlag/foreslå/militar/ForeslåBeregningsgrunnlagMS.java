package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.militar;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagMS.ID)
class ForeslåBeregningsgrunnlagMS extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 32.6";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for MS til 3G.";

    ForeslåBeregningsgrunnlagMS() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus andel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS);

        BigDecimal bruttoPåAndelenFørRegel = Optional.ofNullable(andel.getBruttoPrÅr()).orElse(BigDecimal.ZERO);
        BigDecimal diffMellomBruttoOgMilitærkrav = grunnlag.getMinsteinntektMilitærHarKravPå().subtract(grunnlag.getBruttoPrÅr());
        BigDecimal nyBeregnet = diffMellomBruttoOgMilitærkrav.compareTo(BigDecimal.ZERO) > 0 ? diffMellomBruttoOgMilitærkrav.add(bruttoPåAndelenFørRegel): bruttoPåAndelenFørRegel;
        BeregningsgrunnlagPrStatus.builder(andel).medBeregnetPrÅr(nyBeregnet).build();

        //Sett hjemmel
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.MS).setHjemmel(BeregningsgrunnlagHjemmel.F_14_7);
        //Regelsporing
        Map<String, Object> resultater = new LinkedHashMap<>();
        resultater.put("beregnetPrÅr", nyBeregnet);
        resultater.put("erFastsattAvSaksbehandler", andel.erFastsattAvSaksbehandler());
        if (andel.erFastsattAvSaksbehandler()) {
            resultater.put("inntektFastsattAvSaksbehandler", bruttoPåAndelenFørRegel);
        }
        return beregnet(resultater);
    }
}
