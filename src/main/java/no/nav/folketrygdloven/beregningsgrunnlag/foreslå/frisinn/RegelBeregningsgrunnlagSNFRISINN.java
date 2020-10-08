package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.selvstendig.FastsettBeregnetPrÅr;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelBeregningsgrunnlagSNFRISINN.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelBeregningsgrunnlagSNFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.7";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> beregnBruttoSN =
            rs.beregningsRegel("FRISINN 2.7", "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende",
                new BeregnBruttoBeregningsgrunnlagSNFRISINN(), new FastsettBeregnetPrÅr());


        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende =
            rs.beregningsRegel("FRISINN 2", "Foreslå beregningsgrunnlag for selvstendig næringsdrivende",
                new FastsettBeregningsperiodeSNFRISINN(), beregnBruttoSN);

        return foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende;
    }
}
