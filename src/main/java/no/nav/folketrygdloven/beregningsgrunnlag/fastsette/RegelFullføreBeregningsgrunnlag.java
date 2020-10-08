package no.nav.folketrygdloven.beregningsgrunnlag.fastsette;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelFastsettUtenAvkortingATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.avkorting.RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.FastsettMaksimalRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g.RegelFastsettAvkortetVedRefusjonOver6G;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.reduksjon.FastsettDagsatsPrAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFullføreBeregningsgrunnlag extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

    public RegelFullføreBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR_6.1 18. Fastsett Reduksjon for hver beregningsgrunnlagsandel og totalt beregningrunnlag etter reduksjon
        // FP_BR_6.2 19. Fastsett dagsats pr beregningsgrunnlagsandel og totalt
        Specification<BeregningsgrunnlagPeriode> fastsettReduksjon = rs.beregningsRegel(ReduserBeregningsgrunnlag.ID, ReduserBeregningsgrunnlag.BESKRIVELSE,
            new ReduserBeregningsgrunnlag(), new FastsettDagsatsPrAndel());

        // 13 Fastsett avkortet BG når refusjon over 6G
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetVedRefusjonOver6G = rs.beregningsRegel(
            RegelFastsettAvkortetVedRefusjonOver6G.ID,
            RegelFastsettAvkortetVedRefusjonOver6G.BESKRIVELSE,
            new RegelFastsettAvkortetVedRefusjonOver6G(regelmodell).getSpecification(),
            fastsettReduksjon);

        // 8. Fastsett avkortet BG når refusjon under 6G
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetVedRefusjonUnder6G = rs.beregningsRegel(
            RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.ID,
            RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.BESKRIVELSE,
            new RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G(regelmodell).getSpecification(),
            fastsettReduksjon);

        // FP_BR_29.7 7. Sjekk om summen av maksimal refusjon overstiger 6G
        Specification<BeregningsgrunnlagPeriode> sjekkMaksimaltRefusjonskrav = rs.beregningHvisRegel(new SjekkSumMaxRefusjonskravStørreEnn6G(),
            fastsettAvkortetVedRefusjonOver6G, fastsettAvkortetVedRefusjonUnder6G);

        // 6. Fastsett uten avkorting
        Specification<BeregningsgrunnlagPeriode> fastsettUtenAvkorting = rs.beregningsRegel("FP_BR_29.6", "Fastsett BG uten avkorting",
            new RegelFastsettUtenAvkortingATFL().getSpecification(), fastsettReduksjon);

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        Specification<BeregningsgrunnlagPeriode> beregnEventuellAvkorting = rs.beregningHvisRegel(new SjekkGradertBeregningsgrunnlagStørreEnnGrenseverdi(), sjekkMaksimaltRefusjonskrav, fastsettUtenAvkorting);

        // FP_BR_29.3 3. For hver beregningsgrunnlagsandel: Fastsett Refusjonskrav for beregnings-grunnlagsandel
        Specification<BeregningsgrunnlagPeriode> fastsettMaksimalRefusjon = rs.beregningsRegel(FastsettMaksimalRefusjon.ID, FastsettMaksimalRefusjon.BESKRIVELSE,
            new FastsettMaksimalRefusjon(), beregnEventuellAvkorting);

        // FP_BR_29.3 3. For hver beregningsgrunnlagsandel: Fastsett Refusjonskrav for beregnings-grunnlagsandel
        Specification<BeregningsgrunnlagPeriode> fastsettBeregningsgrunnlag = rs.beregningsRegel(FastsettIkkeSøktForTil0.ID, FastsettIkkeSøktForTil0.BESKRIVELSE,
            List.of(new FastsettIkkeSøktForTil0(), new FastsettMaksimalRefusjon()), beregnEventuellAvkorting);

        return fastsettBeregningsgrunnlag;
    }
}
