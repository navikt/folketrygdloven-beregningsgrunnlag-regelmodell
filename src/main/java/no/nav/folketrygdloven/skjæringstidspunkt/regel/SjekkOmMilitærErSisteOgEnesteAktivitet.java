package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet.MILITÆR_ELLER_SIVILTJENESTE;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMilitærErSisteOgEnesteAktivitet.ID)
class SjekkOmMilitærErSisteOgEnesteAktivitet extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.8";
    static final String BESKRIVELSE = "Er siste aktivitet før skjæringstidspunkt for opptjening, militær eller obligatorisk" +
        " sivilforvarstjeneste og er dette brukers eneste aktivitet på dette tidspunktet og har bruker andre aktiviteter i opptjeningsperioden?";

    SjekkOmMilitærErSisteOgEnesteAktivitet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<Aktivitet> sisteAktiviteter = aktivePerioder.stream()
            .filter(ap -> !ap.getPeriode().getTom().isBefore(regelmodell.sisteAktivitetsdato()))
            .map(AktivPeriode::getAktivitet)
            .collect(Collectors.toList());

        return sisteAktiviteter.size() == 1
            && sisteAktiviteter.contains(MILITÆR_ELLER_SIVILTJENESTE)
            && aktivePerioder.stream().anyMatch(ap -> !MILITÆR_ELLER_SIVILTJENESTE.equals(ap.getAktivitet()))
            ? ja() : nei();

    }
}
