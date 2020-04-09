package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.BeregnSelvstendigAndelForKombinasjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

import java.util.Arrays;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 2.8";

    public RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagKombinasjon = rs.beregningsRegel("FP_BR_14-15-27-28", "Beregn beregningsgrunnlag for arbeidstaker/frilanser)",
                new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification(), new RegelBeregningsgrunnlagSNFRISINN().getSpecification());

        return beregningsgrunnlagKombinasjon;
    }
}
