package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.avkorting;

import java.util.Arrays;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettUtbetalingsbeløpTilBruker.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/FP_BR+29+-+Avkorte+beregningsgrunnlag")
public class RegelFastsettUtbetalingsbeløpTilBruker extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.8 6-9";
    static final String BESKRIVELSE = "Regelen skal fastsette beløp som skal utbetales direkte til bruker, pr. arbeidsforhold.";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR_29.8.9 For hvert arbeidsforhold der beregningsgrunnlagsandelen ikke er fordelt: Vurder om andeler er ferdig fordelt og oppdater fordelt til bruker
        Specification<BeregningsgrunnlagPeriode> vurderOmAndelerErFerdigFordelt = new VurderOmAndelerErFerdigFordeltOgOppdaterFordeltTilBruker();

        // FP_BR_29.8.6 - 8 Fastsett andel til fordeling, beregn prosentandel og fastsett brukers andel
        List<Specification<BeregningsgrunnlagPeriode>> liste = Arrays.asList(new FastsettAndelTilFordeling(),
                new BeregnProsentvisAndel(),
                new FastsettBrukersAndelFraArbeidsforholdSomIkkeErFordelt());
        Specification<BeregningsgrunnlagPeriode> fastsattUtbetalingsbeløpTilBruker = rs.beregningsRegel(ID, BESKRIVELSE, liste, vurderOmAndelerErFerdigFordelt);

        return fastsattUtbetalingsbeløpTilBruker;
    }
}
