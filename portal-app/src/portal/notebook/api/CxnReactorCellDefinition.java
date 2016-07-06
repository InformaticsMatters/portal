package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.CxnReactor;
import org.squonk.jobdef.JobDefinition;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class CxnReactorCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "ChemAxonReactor";
    private final static long serialVersionUID = 1l;

    private static String INPUT_R1 = "Reactant1";
    private static String INPUT_R2 = "Reactant2";

    private static final String[] REACTIONS = new String[] {
            "1,2,4-Oxadiazole formation",
            "1,2,4-Triazole formation from nitrile and hydrazide",
            "2,3-Dihydro benzimidazole formation from diamine and carbonyls",
            "Acylation of alcohols with phosgene",
            "Acylation of nucleophiles with carboxylic acid anhydrides",
            "Acylation_of_amines",
            "Acylation_of_nucleophiles_with_acid_chlorides",
            "Acylation_of_nucleophiles_with_carboxylic_acids",
            "Addition of alcohols to alkynes",
            "Addition of hydrogen cyanide to alkynes",
            "Aldol_reaction",
            "Alkyaltion_of_amine_with_alkyl_halide",
            "Alkyl Lithium formation from alkyl halides",
            "Alkylation of phenols with benzyl chloride",
            "Allylic halogenation of alkenes",
            "Alpha-Alkylation of beta-keto esters",
            "Alpha-alkylation of esters",
            "Alpha_halogenation_of_carboxylic_acids",
            "Amide synthesis from carboxylic acid",
            "Amine sulfonation",
            "Arylation_of_amines",
            "Baylis-Hillman vinyl alkylation",
            "Benzidine rearrangement",
            "Benzilic type rearrangement",
            "Benzimidazole formation from 1,2-phenyldiamine and carbonyls",
            "Benzothiazole formation from 2-aminothiophenol and aldehyde",
            "Benzoxazole formation from 2-aminophenol and carbonyls",
            "Beta HX elimination (Zaitsev elimination)",
            "Birch reduction",
            "Bromination of aliphatic nitro compounds",
            "Carbamate formation from isocyanate",
            "Carboxylic acid addition to alkynes (Markovnikov)",
            "Carboxylic acid addition to alkynes (anti-Markovnikov)",
            "Catalytic hydration of alkenes",
            "Catalytic hydration of nitriles to amides",
            "Catalytic hydrogenation of aromatic compounds with Rh_C or Pt_C",
            "Catalytic hydrogenation of aromatic hydrocarbons with H2-Ni or H2-Pd_C",
            "Catalytic hydrogenation of phenols",
            "Catalytic_conversion_of_arylhalides_to_anilines",
            "Catalytic_conversion_of_arylhalides_to_phenols",
            "Chan reduction of acetylenes",
            "Cleavage of acyclic ethers with HI (SN1 mechanism)",
            "Cleavage of epoxides with hot water",
            "Cleavage of ethers with HI (SN2 mechanism)",
            "Cleavage of vinyl ethers",
            "Clemmensen_reduction",
            "Condensation_reaction_of_primary_amines_with_oxocompounds",
            "Condensation_reaction_of_secondary_amines_with_oxocompounds",
            "Conversion of carbamyl chlorides to isocyanates",
            "Corey-Chaykovsky reaction",
            "Corey-Kim oxidation of alcohols",
            "Cross_Claisen_condensation",
            "Cyanation of primary amines",
            "Decarboxylative coupling for biaryl synthesis",
            "Dehalogenation of vicinal dihalides",
            "Dehydration of alcohols to alkenes",
            "Dehydration of amides to nitriles",
            "Dess-Martin oxidation of alcohols",
            "Diazotisation_of_primary_anilines",
            "Dieckman condensation of diesters",
            "Diels-Alder cycloaddition",
            "Diels-Alder reaction with fused aromatic hydrocarbons",
            "Dihydroimidazole thione formation from alpha-aminocarbonyl and tiocyanate",
            "Direct_alkylation_of_amines_with_epoxide",
            "Doering-LaFlamme allene synthesis",
            "Esterification of alcohols",
            "Ether formation from alcohols",
            "Fischer indole synthesis",
            "Fischer_esterification",
            "Formation_of_acetals_and_ketals",
            "Formation_of_cyclic_acetals_and_ketals",
            "Friedel-Crafts acylation of heteroaromatics",
            "Friedel-Crafts acylation",
            "Friedel-Crafts alkylation",
            "Friedlander quinoline synthesis",
            "Fries rearrangement of aryl esters",
            "Fritsch-Buttenberg-Wiechell acetylene synthesis (rearrangement)",
            "Furan formation from beta-ketoester and alpha-halocarbonyls",
            "Gattermann-Koch synthesis of aldehydes",
            "Grignard addition to CO2",
            "Grignard addition to carbonyl compounds",
            "Grignard_addition_to_carbonyl_compounds",
            "Grignard_reagent_formation",
            "Guanidine formation from carbodiimide and primary amines",
            "Guanidine formation from carbodiimide and secondary amines",
            "Guareschy_Thorpe_pyridone_synthesis",
            "Halogen addition to alkenes",
            "Halogen addition to alkynes",
            "Halogenation of alcohols with hydrogen halides",
            "Halogenation of alkanes (UV light)",
            "Halogenation of alkanes (substitution)",
            "Halogenation of aromatic hydrocarbons",
            "Hantzsch pyrrole synthesis",
            "Hantzsch thiazole synthesis",
            "Hantzsch_pyridine_synthesis",
            "Heck reaction (intermolecular)",
            "Heck reaction (intramolecular)",
            "Hinsberg thiophene synthesis",
            "Hofman carbylamine reaction",
            "Hofmann amide degradation",
            "Hofmann_Loffler_Freytag_reaction",
            "Hofmann_elimination",
            "Horner_Wadsworth_Emmons_reaction",
            "Huisgen 1,4-disubstituted triazole synthesis",
            "Huisgen 1,5-disubstituted triazole synthesis",
            "Huisgen triazole synthesis",
            "Hydration of alkenes",
            "Hydration of alkynes (Markovnikov's addition)",
            "Hydration of terminal alkynes (anti-Markovnikov's addition)",
            "Hydroformylation of alkenes to aldehydes",
            "Hydrogen bromide addition to alkenes (anti-Markovnikov)",
            "Hydrogen halide addition to alkenes (Markovnikov)",
            "Hydrogen halide addition to alkynes",
            "Hydrogenation of oxompounds to alcohols",
            "Hydrolysis of alpha-haloacids to alpha-hydroxyacids",
            "Hydrolysis of esters to carboxylic acids",
            "Hydrolysis of nitriles to carboxylic acids",
            "Hydrolytic carbon monoxide addition to alkynes",
            "Hydroxylation of alkenes with potassium permanganate",
            "Hydroxymethylation of phenols",
            "Imidazole formation from 1,2-diketone and aldehyde",
            "Imidazole formation from alpha-haloketone and amidine",
            "Intramolecular condensation of dicarboxylic acids to anhydrides",
            "Intramolecular_amine_alkylation",
            "Isomerisation of alkenes",
            "Isothiocyanate_formation_from_primary_or_secondary_amines",
            "Jones oxidation of primary alcohols to carboxylic acids",
            "Jones oxidation of secondary alcohols to ketones",
            "Ketone formation from acid chloride and Grignard reagent",
            "Knorr_pyrrole_synthesis",
            "Kolbe-Schmitt synthesis",
            "Malonic acid decarboxylation",
            "Meerwein_Ponndorf_Verley_reduction",
            "Menshutkin_reaction",
            "Methylation of phenols with diazomethane",
            "Milas olefin hydroxylation",
            "Mitsunobu_reaction_with_nucleophiles",
            "N_acylation_of_azides",
            "Negishi_coupling",
            "Niementowski_quinazoline_synthesis",
            "Nitration of alcohols with nitric acid",
            "Nitration of alkanes",
            "Nitration of aromatic hydrocarbons",
            "Nitration of furane pyrrole thiophene",
            "Nitration_of_aromatic_amines",
            "Nitroalkane formation",
            "Nitrosation_of_secondary_amines",
            "Nucleophile acylation with carboxylic acid or anhydride",
            "Nucleophilic aromatic substitution (SN2)",
            "O-Alkylisourea formation from carbodiimide",
            "Oppenauer oxidation",
            "Oxazole formation from beta-ketoester and amine",
            "Oxazole formation from propargyl alcohol and amide",
            "Oxidation fo dicarboxylic acids to cyclic ketones",
            "Oxidation of alcohols to aldehydes or ketones",
            "Oxidation of alcohols to carboxylic acids",
            "Oxidation of alcohols",
            "Oxidation of alkanes to alcohols",
            "Oxidation of alkanes to ketones",
            "Oxidation of alkenes with peracids",
            "Oxidation of alkenes with potassium permanganate",
            "Oxidation of alkynes with potassium permanganate(basic medium)",
            "Oxidation of alkynes with potassium permanganate(neutral medium)",
            "Oxidation of alpha methyl aromatic hydrocarbons with KMnO4 to benzoic acid",
            "Oxidation of phenols to ortho or para quinones",
            "Oxidation of vicinal diols to aldehydes or ketones",
            "Oxidation_of_aldehydes",
            "Oxidation_of_anilines_to_ortho_quinones",
            "Oxidation_of_anilines_to_para_quinones",
            "Oxidation_of_tertiary_amines_to_amine_oxides",
            "Ozonolysis of alkenes",
            "Paal-Knorr pyrrole synthesis",
            "Paal-Knorr thiophene synthesis",
            "Petasis_reaction",
            "Phenol_formation_from_primary_anilines",
            "Phthalazinone formation from phthtalaldehyde acid and hydrazine",
            "Pictet-Spengler isoquinoline synthesis",
            "Pinner_reaction",
            "Pyrazine formation from 1,2-dione and 1,2-diamine",
            "Pyrazine formation from alpha-aminoketone",
            "Pyrazole and isoxazole formation from 1,3-dicarbonyls",
            "Pyrimidine formation from 1,3-diketone and guanidine",
            "Quinazolinone chlorination",
            "Reaction of alkynes with oxo compounds (Reppe like reaction)",
            "Reaction of primary amines with phosgene",
            "Reaction_of_amines_with_alcohols",
            "Reaction_of_primary_aliphatic_amines_with_nitrous_acid",
            "Reduction of alkyl halides (ZnH+_LiAlH4_PdC_HI)",
            "Reduction of amides to aldehydes",
            "Reduction of carboxylic acids to alcohols",
            "Reduction of esters to alcohols with LiAlH4",
            "Reduction of esters to aldehydes with DIBAL-H",
            "Reduction of nitriles to amines",
            "Reduction of nitrils to aldehydes",
            "Reduction of nitro arenes to azo arenes",
            "Reduction of nitro compounds to amines or anilines",
            "Reduction of oxocompounds to alcohols with NaBH4",
            "Reduction of oxocompounds to alcohols",
            "Reformatsky reaction to form beta-hydroxy esters",
            "Reimer-Tiemann formylation of phenols and pyrrols",
            "Ring closure by halogen elimination from alkyl halides",
            "Ritter reaction of alkenes",
            "Rosenmund reduction of acid chlorides",
            "SN1 substitution of alkyl halides",
            "SN2 substitution of alkyl halides",
            "Schotten-Baumann reaction with phenols",
            "Schotten_Baumann_reaction",
            "Sonogashira_coupling",
            "Spirochromanone formation from 2-hydroxyacetophenone and cycloalkanone",
            "Sulfation of alcohols with sulfuric acid",
            "Sulfonation of aromatic hydrocarbons",
            "Sulfonation_of_aromatic_amines",
            "Suzuki_coupling",
            "Swern oxidation of alcohols",
            "Symmetric coupling of 1-alkynes",
            "Symmetric coupling of di-alkynes",
            "Synthesis_of_acid_bromides",
            "Synthesis_of_acid_chlorides",
            "Synthesis_of_amides_from_carboxylic_acids",
            "Synthesis_of_carboxylic_acid_anhydrides",
            "Synthesis_of_cyclic_anhydrides",
            "Tetrazole formation from nitrile and azide",
            "Tosylation with Koser's reagent",
            "Transesterification_reaction",
            "Ullmann reaction",
            "Vilsmeier aldehyde synthesis",
            "Wacker-Tsuji olefine oxidation",
            "Water_addition_to_carbonyl_compounds",
            "Williamson_ether_synthesis",
            "Wittig_reaction",
            "Wolff_Kishner_reduction",
            "Wurtz reaction",
            "alpha_Bromination_of_oxocompounds",
            "alpha_Chlorination_of_oxocompounds",
            "alpha_Hydroxynitrile_formation",
            "alpha_Iodination_of_oxocompounds",
            "cross-Cannizzaro_reaction",
            "von_Braun_degradation_of_tertiary_amines"
    };

    public CxnReactorCellDefinition() {
        super(CELL_NAME, "Reaction enumeration", "icons/molecule_generator.png", new String[]{"enumeration", "reaction", "library", "dataset"});

        getBindingDefinitionList().add(new BindingDefinition(INPUT_R1, "Reactants 1", VariableType.DATASET));
        getBindingDefinitionList().add(new BindingDefinition(INPUT_R2, "Reactants 2", VariableType.DATASET));

        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));

        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, CxnReactor.OPTION_REACTION, "Reaction", "Reaction from the ChemAxon reaction library")
            .withValues(REACTIONS)
            .withMinMaxValues(1,1));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_REACTIVITY, "Ignore reactivity rules", "Ignore reactivity rules when reacting"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_SELECTIVITY, "Ignore selectivity rules", "Ignore selectivity rules when reacting"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_TOLERANCE, "Ignore tolerance rules", "Ignore tolerance rules when reacting"));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            StepDefinition step1 = new StepDefinition(CxnReactor.CLASSNAME)
                    .withInputVariableMapping(CxnReactor.VARIABLE_R1, createVariableKey(cell, INPUT_R1))
                    .withInputVariableMapping(CxnReactor.VARIABLE_R2, createVariableKey(cell, INPUT_R2))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));


            return buildJobDefinition(cellExecutionData, cell, step1);
        }
    }

}
