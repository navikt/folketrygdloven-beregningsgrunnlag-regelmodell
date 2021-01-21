package no.nav.folketrygdloven.beregningsgrunnlag.vurder.omp;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.AvslagUnderEnHalvG;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.SjekkBeregningsgrunnlagMindreEnn;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelVurderBeregningsgrunnlagOMP extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

    public RegelVurderBeregningsgrunnlagOMP(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_VK_32.2 2. Opprett regelmerknad (avslag)
        Specification<BeregningsgrunnlagPeriode> avslagUnderEnHalvG = new AvslagUnderEnHalvG();

        // FP_VK_32.1 1. Brutto BG > 0,5G ?
        Specification<BeregningsgrunnlagPeriode> sjekkOmBGUnderHalvG = rs.beregningHvisRegel(new SjekkBeregningsgrunnlagMindreEnn(),
            avslagUnderEnHalvG, new Beregnet());

	    // K9_9.3.2 Søkt full refusjon i periode?
	    Specification<BeregningsgrunnlagPeriode> sjekkOmDirekteUtbetaling = rs.beregningHvisRegel(new ErDirekteUtbetalingTilBruker(),
			    sjekkOmBGUnderHalvG, new Beregnet());

        return sjekkOmDirekteUtbetaling;
    }
}
