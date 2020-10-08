package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */


@RuleDocumentation(value = RegelFastsettSkjæringstidspunkt.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=234395762")
public class RegelFastsettSkjæringstidspunkt implements RuleService<AktivitetStatusModell> {

    public static final String ID = "FP_BR_21";

	@Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {

        Ruleset<AktivitetStatusModell> rs = new Ruleset<>();

//      FP_BR 21.5 Skjæringstidspunkt for beregning = første dag etter siste aktivitetsdag før skjæringstidspunkt for opptjening

        Specification<AktivitetStatusModell> førsteDagEtterAktivitet = new FastsettSkjæringstidspunktEtterAktivitet();

//      FP_BR 21.6 Skjæringstidspunkt for beregning = skjæringstidspunkt for opptjening

        Specification<AktivitetStatusModell> likSkjæringstidspunktForOpptjening = new FastsettSkjæringstidspunktLikOpptjening();

//      FP_BR 21.8 Skjæringstidspunkt for beregning = Første dag etter den siste dagen med aktivitet som ikke er militær eller obligatorisk sivilforsvarstjeneste

        Specification<AktivitetStatusModell> førsteDagEtterAktivitetSomIkkeErMilitær = new FastsettSkjæringstidspunktEtterAktivitetSomIkkeErMilitær();

//      FP_BR 21.1 Er tom-dato for siste aktivitet dagen før skjæringstidspunkt for opptjening?

        Specification<AktivitetStatusModell> erDetAktivitetFremTilStpForOpptjening =
                rs.beregningHvisRegel(new SjekkOmAktivitetRettFørOpptjening(), likSkjæringstidspunktForOpptjening, førsteDagEtterAktivitet);

//      FP_BR 21.8 Er siste aktivitet før skjæringstidspunkt for opptjening, militær eller obligatorisk sivilforvarstjeneste og er dette brukers eneste aktivitet på dette tidspunktet Og har bruker andre aktiviteter i opptjeningsperioden

        Specification<AktivitetStatusModell> startFastsettSkjæringstidspunktForBeregning =
            rs.beregningHvisRegel(new SjekkOmMilitærErSisteOgEnesteAktivitet(), førsteDagEtterAktivitetSomIkkeErMilitær, erDetAktivitetFremTilStpForOpptjening);

        return startFastsettSkjæringstidspunktForBeregning;
    }
}
