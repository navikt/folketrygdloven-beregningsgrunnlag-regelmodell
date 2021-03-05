package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelEndring;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

public class IdentifiserPerioderForEndringIUtbetalingsgrad {
    private IdentifiserPerioderForEndringIUtbetalingsgrad() {
        // skjul public constructor
    }

	// TODO: Reduser kompleksitet
    public static Set<PeriodeSplittData> identifiser(AndelEndring endringIUtbetalingsgrad) {
        Set<PeriodeSplittData> set = new HashSet<>();
        List<Gradering> graderinger = endringIUtbetalingsgrad.getGraderinger();
        for (int i = 0; i < graderinger.size(); i++) {
            Gradering curr = graderinger.get(i);
            if (i > 0) {
                Gradering prev = graderinger.get(i - 1);
                if (curr.getUtbetalingsprosent().compareTo(prev.getUtbetalingsprosent()) != 0 || !curr.getPeriode().getFom().minusDays(1).equals(prev.getTom())) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getFom());
                    set.add(periodeSplitt);
                }
            } else {
                if (curr.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) != 0) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getFom());
                    set.add(periodeSplitt);
                }
            }
            if (i < graderinger.size() - 1) {
                Gradering next = graderinger.get(i + 1);
                if (next.getPeriode().getFom().isAfter(curr.getTom().plusDays(1)) && curr.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) != 0) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getTom().plusDays(1));
                    set.add(periodeSplitt);
                }
            } else if (curr.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) != 0) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getTom().plusDays(1));
                    set.add(periodeSplitt);
            }
        }
        return set.stream().sorted(Comparator.comparing(PeriodeSplittData::getFom)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static PeriodeSplittData lagPeriodeSplitt(LocalDate fom) {
        return PeriodeSplittData.builder()
            .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)
            .medFom(fom)
            .build();
    }
}
