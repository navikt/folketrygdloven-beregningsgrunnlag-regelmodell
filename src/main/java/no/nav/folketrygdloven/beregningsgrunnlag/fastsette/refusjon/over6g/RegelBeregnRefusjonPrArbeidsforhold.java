package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregnRefusjonPrArbeidsforhold.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class RegelBeregnRefusjonPrArbeidsforhold extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.1-3";
    static final String BESKRIVELSE = "Beregn arbeidsgivers andel av det som skal refunderes";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        return rs.beregningsRegel(ID, BESKRIVELSE,
                Arrays.asList(new BeregnArbeidsgiversAndeler(), new BeregnAvkortetRefusjon()),
                new VurderOmAlleFerdig());
    }
}
