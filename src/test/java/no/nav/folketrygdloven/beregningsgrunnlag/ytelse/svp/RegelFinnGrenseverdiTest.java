package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelFinnGrenseverdiTest {

    public static final String ORGNR = "910";
    private static final String ORGNR_2 = "974760673";
    private static final String ORGNR_3 = "976967631";

    @Test
    public void ett_arbeidsforhold_under_6G() {
        //Arrange
        double beregnetPrÅr = 400_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(beregnetPrÅr));
    }

    @Test
    public void ett_arbeidsforhold_under_6G_ikkje_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 400_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
    }

    @Test
    public void ett_arbeidsforhold_over_6G() {
        //Arrange
        double beregnetPrÅr = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void to_arbeidsforhold_under_6G() {
        //Arrange
        double beregnetPrÅr = 250_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(550_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G() {
        //Arrange
        double beregnetPrÅr = 350_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void to_arbeidsforhold_under_6G_søkt_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 250_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(250_000));
    }

    @Test
    public void to_arbeidsforhold_til_sammen_over_6G_søkt_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 500_000;
        double beregnetPrÅr2 = 500_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void to_arbeidsforhold_den_ene_over_6G_søkt_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(480_000));
    }

    @Test
    public void to_arbeidsforhold_under_6G_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 250_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_for_begge_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 600_000;
        double beregnetPrÅr2 = 600_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(450_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(540_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_en_full_ytelse_for_den_over_6G() {
        //Arrange
        double beregnetPrÅr = 600_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }


    @Test
    public void to_arbeidsforhold_under_6G_delvis_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 250_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(275_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_for_begge_delvis_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 600_000;
        double beregnetPrÅr2 = 600_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 600_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void to_arbeidsforhold_over_6G_delvis_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 250_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(275_000));
    }


    @Test
    public void tre_arbeidsforhold_over_6G_søkt_ytelse_for_en() {
        //Arrange
        double beregnetPrÅr = 300_000;
        double beregnetPrÅr2 = 300_000;
        double beregnetPrÅr3 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
    }

    @Test
    public void tre_arbeidsforhold_over_6G_søkt_ytelse_for_to() {
        //Arrange
        double beregnetPrÅr = 300_000;
        double beregnetPrÅr2 = 300_000;
        double beregnetPrÅr3 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);
        leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }

    @Test
    public void tre_arbeidsforhold_over_6G_delvis_søkt_ytelse_for_en_full_for_en() {
        //Arrange
        double beregnetPrÅr = 300_000;
        double beregnetPrÅr2 = 300_000;
        double beregnetPrÅr3 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);
        leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void tre_arbeidsforhold_over_6G_søkt_ytelse_for_alle() {
        //Arrange
        double beregnetPrÅr = 300_000;
        double beregnetPrÅr2 = 300_000;
        double beregnetPrÅr3 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);
        leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 100);


        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void frilans_under_6G_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void frilans_under_6G_ikkje_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
    }

    @Test
    public void frilans_over_6G_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void frilans_under_6G_delvis_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
    }

    @Test
    public void frilans_over_6G_delvis_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 50);


        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }


    @Test
    public void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 300_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_kun_frilans() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_kun_arbeid() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 300_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_til_sammen_søkt_ytelse_for_kun_frilans() {
        //Arrange
        double beregnetPrÅr = 500_000;
        double beregnetPrÅr2 = 500_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_til_sammen_søkt_ytelse_for_kun_arbeid() {
        //Arrange
        double beregnetPrÅr = 500_000;
        double beregnetPrÅr2 = 500_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_kun_frilans() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
    }

    @Test
    public void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_kun_arbeid() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }


    @Test
    public void næring_under_6G_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
    }

    @Test
    public void næring_under_6G_søkt_delvis_ytelse() {
        //Arrange
        double beregnetPrÅr = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 50);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    @Test
    public void næring_under_6G_ikkje_søkt_ytelse() {
        //Arrange
        double beregnetPrÅr = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
    }

    @Test
    public void næring_og_frilans_under_6G_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }

    @Test
    public void næring_og_frilans_over_6G_søkt_ytelse_for_begge() {
        //Arrange
        double beregnetPrÅr = 500_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void næring_og_frilans_over_6G_for_næring_søkt_ytelse_for_næring() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }

    @Test
    public void næring_og_frilans_over_6G_for_frilans_søkt_ytelse_for_næring() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
    }

    @Test
    public void næring_og_frilans_over_6G_for_frilans_søkt_ytelse_for_frilans() {
        //Arrange
        double beregnetPrÅr = 200_000;
        double beregnetPrÅr2 = 800_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 0);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_alle() {
        //Arrange
        double beregnetPrÅr = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr, 100);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_næring() {
        //Arrange
        double beregnetPrÅr = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_arbeid() {
        //Arrange
        double beregnetPrÅr = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 0);
        leggTilFrilans(periode, 2L, beregnetPrÅr, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_ytelse_for_arbeid() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 100_000;
        double beregnetPrÅr3 = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 0);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 100);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_ytelse_for_næring() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 100_000;
        double beregnetPrÅr3 = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 100);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
    }

    @Test
    public void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_delvis_ytelse_for_næring() {
        //Arrange
        double beregnetPrÅr = 800_000;
        double beregnetPrÅr2 = 100_000;
        double beregnetPrÅr3 = 100_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 50);
        leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
    }

    @Test
    public void næring_og_arbeidsforhold_over_6G_til_sammen_søkt_delvis_ytelse_for_næring_rest_etter_arbeid_mindre_enn_bg_for_frilans() {
        //Arrange
        double beregnetPrÅr = 500_000;
        double beregnetPrÅr2 = 200_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
    }

    @Test
    public void næring_og_arbeidsforhold_over_6G_til_sammen_søkt_delvis_ytelse_for_næring_rest_etter_arbeid_mindre_enn_gradert_bg_for_frilans() {
        //Arrange
        double beregnetPrÅr = 500_000;
        double beregnetPrÅr2 = 400_000;

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
            .build();

        Beregningsgrunnlag.builder()
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(BigDecimal.valueOf(100_000));

        leggTilNæring(periode, 1L, beregnetPrÅr, 50);
        leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

        //Act
        kjørRegel(periode);

        assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode periode) {
        RegelFinnGrenseverdi regel = new RegelFinnGrenseverdi(periode);
        Evaluation evaluation = regel.evaluer(periode);
        return RegelmodellOversetter.getRegelResultat(evaluation, "input");
    }



    private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
                                       long andelsnr,
                                       String orgnr,
                                       double beregnetPrÅr,
                                       double utbetalingsgrad) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        if (atfl == null) {
            BeregningsgrunnlagPeriode.builder(periode)
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
                    .builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, 100_000, utbetalingsgrad, arbeidsforhold))
                    .build());
        } else {
            BeregningsgrunnlagPrStatus.builder(atfl)
                .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, 100_000, utbetalingsgrad, arbeidsforhold))
                .build();
        }
    }

    private void leggTilFrilans(BeregningsgrunnlagPeriode periode,
                                long andelsnr,
                                double beregnetPrÅr, double utbetalingsgrad) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        if (atfl == null) {
            BeregningsgrunnlagPeriode.builder(periode)
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
                    .builder()
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, 0, utbetalingsgrad, arbeidsforhold))
                    .build());
        } else {
            BeregningsgrunnlagPrStatus.builder(atfl)
                .medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, 0, utbetalingsgrad, arbeidsforhold))
                .build();
        }
    }

    private void leggTilNæring(BeregningsgrunnlagPeriode periode,
                               long andelsnr,
                               double beregnetPrÅr, double utbetalingsgrad) {
            BeregningsgrunnlagPrStatus status = BeregningsgrunnlagPrStatus
                .builder()
                .medAndelNr(andelsnr)
                .medAktivitetStatus(AktivitetStatus.SN)
                .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
                .medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
                .build();
            status.setErSøktYtelseFor(utbetalingsgrad > 0);
            BeregningsgrunnlagPeriode.builder(periode)
                .medBeregningsgrunnlagPrStatus(status);
    }

    private BeregningsgrunnlagPrArbeidsforhold lagBeregningsgrunnlagPrArbeidsforhold(long andelsnr,
                                                                                     double beregnetPrÅr,
                                                                                     double refusjonskrav,
                                                                                     double utbetalingsgrad,
                                                                                     Arbeidsforhold arbeidsforhold) {
        BeregningsgrunnlagPrArbeidsforhold arb = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(andelsnr)
            .medArbeidsforhold(arbeidsforhold)
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
            .medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(refusjonskrav))
            .medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
            .build();
        arb.setErSøktYtelseFor(utbetalingsgrad > 0);
        return arb;
    }


}
