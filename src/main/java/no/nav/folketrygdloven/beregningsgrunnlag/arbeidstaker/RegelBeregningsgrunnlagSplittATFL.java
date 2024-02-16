package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregningsgrunnlagSplittATFL implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagSplittATFL(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR 28.5 Fastsett sammenligningsgrunnlag og sjekk avvik for arbeidstaker

        Specification<BeregningsgrunnlagPeriode> beregnSammenligningsgrunnlagAT = new RegelSammenligningsgrunnlagAT().getSpecification();


        // FP_BR 28.4 Er bruker arbeidstaker?

        Specification<BeregningsgrunnlagPeriode> skalSammenligningsgrunnlagOpprettesForArbeidstaker =
            rs.beregningHvisRegel(new SjekkOmArbeidstakerArbeidsforholdFinnes(), beregnSammenligningsgrunnlagAT, new Beregnet());


        // FP_BR 28.1 Fastsett sammenligningsgrunnlag og sjekk avvik for frilans

        Specification<BeregningsgrunnlagPeriode> beregnSammenligningsgrunnlagFL =
            rs.beregningsRegel("FP_BR 28.1", "Fastsett sammenligningsgrunnlag for FL",
                new RegelSammenligningsgrunnlagFL().getSpecification(), skalSammenligningsgrunnlagOpprettesForArbeidstaker);


        // FP_BR 28.0 Er bruker frilans?

        Specification<BeregningsgrunnlagPeriode> skalSammenligningsgrunnlagOpprettesForFrilanser =
            rs.beregningHvisRegel(new SjekkOmFrilansArbeidsforholdFinnes(), beregnSammenligningsgrunnlagFL, skalSammenligningsgrunnlagOpprettesForArbeidstaker);


        // FP_BR 27.9 Er frilans og arbeidstaker i samme organisasjon?

        Specification<BeregningsgrunnlagPeriode> skalArbeidstakerOgFrilanserAvvikvurderes =
            rs.beregningHvisRegel(new SjekkOmFrilansOgArbeidstakerISammeOrganisasjon(), new Beregnet(), skalSammenligningsgrunnlagOpprettesForFrilanser);

        // FP_BR 27.2 Skal vi sammeligne inntekt mot sammenligningsgrunnlaget?

        Specification<BeregningsgrunnlagPeriode> skalÅrsinntektVurderesMotSammenligningsgrunnlaget =
            rs.beregningHvisRegel(new SkalSjekkeÅrsinntektMotSammenligningsgrunnlag(), skalArbeidstakerOgFrilanserAvvikvurderes, new Beregnet());


        // Første beregningsgrunnlagsperiode? Sjekk om vi skal fastsette sammenligninggrunnlag og sjekke det rapportert inntekt

        Specification<BeregningsgrunnlagPeriode> sjekkOmFørstePeriode =
            rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), skalÅrsinntektVurderesMotSammenligningsgrunnlaget, new Beregnet());


        // Har bruker kombinasjonsstatus?

        Specification<BeregningsgrunnlagPeriode> sjekkHarBrukerKombinasjonsstatus =
            rs.beregningHvisRegel(new SjekkHarBrukerKombinasjonsstatus(), new Beregnet(), sjekkOmFørstePeriode);


        // FP_BR 14.3 14.5 14.6 28.4 Beregnet pr år = sum alle inntekter

        Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr =
            rs.beregningsRegel("FP_BR 14.5", "Fastsett beregnet pr år for ATFL",
                new FastsettBeregnetPrÅr(), sjekkHarBrukerKombinasjonsstatus);


        // For hver arbeidsgiver eller frilansinntekt: Fastsett brutto pr periodetype
        // FB_BR 14.3 28.2 Brutto pr periodetype = snitt av fastsatte inntekter av A-ordning * 12
        // FP_BR 15.2 Brutto pr periode_type = inntektsmelding sats * 12
        // FP_BR 15.1 Foreligger inntektsmelding?

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagATOgFL =
            rs.beregningsRegel("FP_BR 14.X", "Fastsett beregningsgrunnlag pr arbeidsforhold",
                RegelBeregnBruttoPrArbeidsforhold.class, regelmodell, "arbeidsforhold", arbeidsforhold, fastsettBeregnetPrÅr);


        // FP_BR X.X Ingen regelberegning hvis besteberegning gjelder

        Specification<BeregningsgrunnlagPeriode> sjekkOmBesteberegning =
            rs.beregningHvisRegel(new SjekkOmBesteberegning(), new Beregnet(), beregningsgrunnlagATOgFL);

        return sjekkOmBesteberegning;
    }
}
