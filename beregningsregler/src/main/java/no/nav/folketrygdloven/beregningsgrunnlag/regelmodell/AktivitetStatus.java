package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.Arrays;
import java.util.List;

/**
 * Intern representasjon av aktivitetstatusene som brukes i beregningskomponenten.
 * Pga avhengigheter mellom beregninger av noen aktivitetstatuser (f. eks AAP/DP alltid før SN/ATFL_SN) er det viktig at
 * rekkefølgen de ulike statusene beregenes overholdes. Denne rekkefølgen blir overholdt av 'prioritet'-feltet. Statuser
 * med høyere prioritet-verdi blir beregnet etter statuser med lavere prioritet-verdi.
 */
public enum AktivitetStatus {

    ATFL("Arbeidstaker/Frilanser", 1, 1),
	AT("Arbeidstaker", 1, 1),
	FL("Frilans", 1, 1),
	KUN_YTELSE("Mottaker av tilstøtende ytelse"),
	DP("Dagpenger", 1, 2),
	SP_AV_DP("Sykepenger av dagpenger", 1, 2),
	PSB_AV_DP("Pleiepenger av dagpenger", 1, 2),
	AAP("Mottaker av arbeidsavklaringspenger", 1, 3),
	BA("Brukers andel"),
	MS("Militær/Sivil", 9, 9),
	UDEFINERT("Udefinert"),
	SN("Selvstendig næringsdrivende", 2, 4),
	ATFL_SN("Kombinasjon av arbeidstaker/frilanser og selvstendig næringsdrivende", 2, 9),
	MIDL_INAKTIV("Midlertidig inaktiv");// 8-47 Midlertidig inaktiv

    private final String beskrivelse;
    //Lavere verdi -> høyere prioritet.
    private final int beregningPrioritet;
    private final int avkortingPrioritet;

    private static final List<AktivitetStatus> ARBEIDSTAKER_STATUSER = Arrays.asList(ATFL, ATFL_SN, AT);    //NOSONAR
    private static final List<AktivitetStatus> FRILANS_STATUSER = Arrays.asList(ATFL, ATFL_SN, FL);    //NOSONAR
    private static final List<AktivitetStatus> SELVSTENDIG_NÆRINGSDRIVENDE_STATUSER = Arrays.asList(SN, ATFL_SN);    //NOSONAR
    private static final List<AktivitetStatus> AAP_OG_DP_STATUSER = Arrays.asList(AAP, DP, SP_AV_DP, PSB_AV_DP);    //NOSONAR
	private static final List<AktivitetStatus> DP_STATUSER = Arrays.asList(DP, SP_AV_DP, PSB_AV_DP);    //NOSONAR
	private static final List<AktivitetStatus> DP_FRA_YTELSE_STATUSER = Arrays.asList(SP_AV_DP, PSB_AV_DP);    //NOSONAR

    AktivitetStatus(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        beregningPrioritet = 1;
        avkortingPrioritet = 9;
    }

    AktivitetStatus(String beskrivelse, int beregningPrioritet, int avkortingPrioritet) {
        this.beskrivelse = beskrivelse;
        this.beregningPrioritet = beregningPrioritet;
        this.avkortingPrioritet = avkortingPrioritet;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public int getBeregningPrioritet() {
        return beregningPrioritet;
    }

    public int getAvkortingPrioritet(){
        return avkortingPrioritet;
    }

    public static boolean erKombinasjonMedSelvstendig(AktivitetStatus aktivitetStatus) {
        return ATFL_SN.equals(aktivitetStatus);
    }

    public static boolean erArbeidstaker(AktivitetStatus aktivitetStatus) {
        return ARBEIDSTAKER_STATUSER.contains(aktivitetStatus);
    }

    public static boolean erFrilanser(AktivitetStatus aktivitetStatus) {
        return FRILANS_STATUSER.contains(aktivitetStatus);
    }

    public static boolean erArbeidstakerEllerFrilanser(AktivitetStatus aktivitetStatus) {
        return erArbeidstaker(aktivitetStatus) || erFrilanser(aktivitetStatus);
    }

    public boolean erSelvstendigNæringsdrivende() {
        return SELVSTENDIG_NÆRINGSDRIVENDE_STATUSER.contains(this);
    }

	public boolean erMilitær() { return MS.equals(this); }

	public boolean erAAPellerDP() {
        return AAP_OG_DP_STATUSER.contains(this);
    }

	public boolean erDP() {
		return DP_STATUSER.contains(this);
	}

	public boolean erDPFraYtelse() {
		return DP_FRA_YTELSE_STATUSER.contains(this);
	}

}
