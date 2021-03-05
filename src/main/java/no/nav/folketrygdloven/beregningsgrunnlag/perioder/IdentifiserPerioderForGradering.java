package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelEndring;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellRefusjonOgNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForGradering {
    private IdentifiserPerioderForGradering() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiser(PeriodeModell input, AndelEndring andelGradering) {
        Set<PeriodeSplittData> set = new HashSet<>();
        andelGradering.getGraderinger().forEach(gradering -> {
            List<PeriodeSplittData> splits = VurderPeriodeForGradering.vurder(input, andelGradering, gradering.getPeriode());
            set.addAll(splits);
        });
        return set;
    }
}
