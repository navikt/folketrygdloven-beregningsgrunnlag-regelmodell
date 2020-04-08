package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

import java.util.List;

public class RegelBeregningsgrunnlagATFLFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagATFLFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr =
            rs.beregningsRegel("FRISINN 2.10", "Fastsett beregnet pr år for ATFL",
                new FastsettBeregnetPrÅrFRISINN(), new Beregnet());

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagATFL =
                rs.beregningsRegel("FRISINN 2.X", "Fastsett beregningsgrunnlag pr arbeidsforhold",
                    RegelBeregnBruttoPrArbeidsforholdFRISINN.class, regelmodell, "arbeidsforhold", arbeidsforhold, fastsettBeregnetPrÅr);

        return beregningsgrunnlagATFL;
    }
}
