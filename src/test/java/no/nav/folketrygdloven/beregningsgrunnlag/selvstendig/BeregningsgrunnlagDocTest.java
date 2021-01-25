package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private BeregningsgrunnlagPeriode regelmodell;

    @BeforeEach
    public void setup() {
        regelmodell = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(skjæringstidspunkt, null))
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medAndelNr(1L)
                        .build())
                .build();
        Beregningsgrunnlag.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medInntektsgrunnlag(new Inntektsgrunnlag())
                .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.SN, null)))
                .medBeregningsgrunnlagPeriode(regelmodell)
                .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), 90000L, 90000L)))
                .build();
    }

    @Test
    public void test_documentation_beregningsgrunnlagSN() { // NOSONAR
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelBeregningsgrunnlagSN().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

    }
}
