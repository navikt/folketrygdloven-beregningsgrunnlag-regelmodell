package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.util.HashMap;
import java.util.Map;

@RuleDocumentation(FastsettBeregnetPrÅrFRISINN.ID)
class FastsettBeregnetPrÅrFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.5";
    static final String BESKRIVELSE = "Beregnet årsinntekt er sum av alle rapporterte inntekter";

    FastsettBeregnetPrÅrFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        BeregningsgrunnlagHjemmel hjemmel = settHjemmelForATFL(grunnlag.getBeregningsgrunnlag(), bgps);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", bgps.getBeregnetPrÅr());
        resultater.put("samletNaturalytelseBortfaltMinusTilkommetPrÅr", bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr());
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }

    private BeregningsgrunnlagHjemmel settHjemmelForATFL(Beregningsgrunnlag grunnlag, BeregningsgrunnlagPrStatus bgps) {
        AktivitetStatusMedHjemmel status = grunnlag.getAktivitetStatus(AktivitetStatus.ATFL);
        boolean kombinasjon = status.getAktivitetStatus().equals(AktivitetStatus.ATFL_SN);
        boolean arbeidstaker = !bgps.getArbeidsforholdIkkeFrilans().isEmpty();
        boolean frilans = bgps.getFrilansArbeidsforhold().isPresent();
        BeregningsgrunnlagHjemmel hjemmel;
        if (arbeidstaker) {
            if (kombinasjon) {
                hjemmel = (frilans ? BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG : BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG);
            } else {
                hjemmel = (frilans ? BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER : BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER);
            }
        } else if (frilans) {
            if (kombinasjon) {
                hjemmel = (BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG);
            } else {
                hjemmel = (BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
            }
        } else {
            throw new IllegalStateException("ATFL-andel mangler både arbeidsforhold og frilansaktivitet");
        }
        status.setHjemmel(hjemmel);
        return hjemmel;
    }
}
