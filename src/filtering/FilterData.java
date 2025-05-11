package filtering;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class FilterData {
    final static String POKEMON_LIST_FILE = "pokemonList.csv";
    final static String POKEMON_LIST_FILE_NO_FORM = "pokemonListNoForm.csv";
    final static String INPUT = "input.csv";

    final static String CURR_PATH = Paths.get("").toAbsolutePath().toString() + "\\src\\filtering\\";
    final static String FEMALE = "♀";
    final static String MALE = "♂";
    final static String[] FILES = {POKEMON_LIST_FILE, POKEMON_LIST_FILE_NO_FORM, INPUT};
    final static String[] CMDS = {"getPokemonList", "learnsMove", "saveInput", "getFiles", "getCommands"};

    final static String[] gens = {"RB","GS","RS","DP","BW","XY","SM","SS", "SV"};

    public static void getPokemonList(boolean withForms) throws IOException {
        URL all_pokemon = new URL("https://pokemondb.net/pokedex/all");

        URLConnection con = all_pokemon.openConnection();
        InputStream is = con.getInputStream();

        String file = CURR_PATH + POKEMON_LIST_FILE;
        if(!withForms) {
            file = CURR_PATH + POKEMON_LIST_FILE_NO_FORM;
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch(IOException e) {
            System.out.println("ERROR: Could not write to file: " + file);
            return;
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = null;
            String prevPokemon = "invalid name";
            writer.write("Number,Name,Type1,Type2\n");

            int counter = 1;
            while ((line = br.readLine()) != null) {
                if(line.contains("alt=") && !line.contains("Pokemon Database logo, with Scizor")) {

                    int name_start = line.indexOf("alt=");
                    String sub = line.substring(name_start + 5);
                    int nextQuote = sub.indexOf("\"");
                    String name = sub.substring(0, nextQuote).replace(" ", "-");
                    name = name.replace(".", "").replace("&#039;", "");
                    name = name.replace("é", "e").replace(":", "");

                    if(name.contains(FEMALE)) {
                        name = name.replace(FEMALE, "-f");
                    }
                    if(name.contains(MALE)) {
                        name = name.replace(MALE, "-m");
                    }
                    if(!withForms) {
                        if(name.contains(prevPokemon)) {
                            continue;
                        } else {
                            prevPokemon = name;
                        }
                        if(name.toLowerCase().contains("form") || name.contains("(")) {
                            name = name.split("-")[0];
                            prevPokemon = name;
                        }
                    }

                    writer.write(counter + ",");
                    writer.write(name + ",");

                    String nextLine = br.readLine();
                    final String TYPE_STR = "href=\"/type/";
                    int type1_start = nextLine.indexOf(TYPE_STR) + TYPE_STR.length();
                    int type1_end = nextLine.indexOf("\"", type1_start);
                    int type2_start = nextLine.indexOf(TYPE_STR, type1_end) + TYPE_STR.length();
                    int type2_end = nextLine.indexOf("\"", type2_start);
                    writer.write(nextLine.substring(type1_start, type1_end) + ",");
                    if(nextLine.indexOf(TYPE_STR, type1_end) == -1) {
                        writer.write("none\n");
                    } else {
                        writer.write(nextLine.substring(type2_start, type2_end) + "\n");
                    }

                    counter++;
                }
            }
        }
        writer.close();
        System.out.println("Successfully wrote to " + file);
    }


    public static String learnsMove(String move) throws IOException {
        //return learnsMove(move, INPUT);
        return smogonMove(move, INPUT);
    }

    //Checks movepool of most recent Gen
    public static String learnsMove(String move, String fileName) throws IOException {
        String pokemon = "";
        String curr_mons = "";

        String file = CURR_PATH + fileName;

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch(IOException e) {
            System.out.println("ERROR: Could not read from file: " + file);
            return "ERROR";
        }
        String pokemonData = null;
        in.readLine();
        final String URL_START = "https://pokemondb.net/pokedex/";
        URL curr;
        while((pokemonData = in.readLine()) != null) {
            String name = pokemonData.split(",")[1].toLowerCase();
            curr = new URL(URL_START + name);
            URLConnection con = curr.openConnection();
            InputStream is;
            try{
                is = con.getInputStream();
            } catch(Exception e) {
                System.out.println("Page Not Found: " + name);
                //return;
                continue;
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = null;
                boolean found_section = false;
                int skipLines = 400;
                int currentLine = 0;
                while (currentLine < skipLines && (line = br.readLine()) != null) {
                    currentLine++;
                }

                while ((line = br.readLine()) != null) {
//                    if(line.contains("Page not found")) {
//                        System.out.println("Page not found for: " + name);
//                        return;
//                    }

                    if(line.contains("<a class=\"sv-tabs-tab active\"") && line.contains("#tab-moves")) {
                        found_section = true;
                    }
                    if(line.contains("</tr></tbody></table></div></div> </div>\t\t\t</div>") && found_section) {
                        break;
                    }
                    if(found_section) {
                        if(line.toLowerCase().contains(move.toLowerCase()) && !pokemon.contains(name)) {
                            pokemon += name + ", ";
                            curr_mons += name + ", ";
                            if(curr_mons.length() > 100) {
                                pokemon += "\n";
                                curr_mons = "";
                            }
                        }
                    }
                    //System.out.println(line);
                }
            }
        }
        return pokemon;
    }

    public static String smogonMove(String move, String fileName) throws IOException {
        String pokemon = "";
        String curr_mons = "";

        String file = CURR_PATH + fileName;

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch(IOException e) {
            System.out.println("ERROR: Could not read from file: " + file);
            return "ERROR";
        }
        String pokemonData = null;
        in.readLine();
        boolean try_again = false;
        int gen = 8;
        while(try_again || (pokemonData = in.readLine()) != null) {
            String name = pokemonData.split(",")[1].toLowerCase();
            if(!try_again) {
                gen = 8;
            } else {
                if(gen == 0) {
                    try_again = false;
                    continue;
                }
                gen--;
            }
            final String URL_START = "https://www.smogon.com/dex/" + gens[gen].toLowerCase() + "/pokemon/";
            URL curr;
            curr = new URL(URL_START + name);
            URLConnection con = curr.openConnection();
            InputStream is;
            try{
                is = con.getInputStream();
            } catch(Exception e) {
                System.out.println("Page Not Found: " + name);
                continue;
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = null;

                while ((line = br.readLine()) != null) {
                    if(line.contains("learnset")) {
                        int start = line.indexOf("learnset");
                        int end = line.indexOf("]", start);
                        if(end-start <= 12) {
                            try_again = true;
                            continue;
                        } else {
                            try_again = false;
                        }
                        String sub = line.substring(start, end);
                        if (sub.toLowerCase().contains(move.toLowerCase()) && !pokemon.contains(name)) {
                            pokemon += name + ", ";
                            curr_mons += name + ", ";
                            if(curr_mons.length() > 100) {
                                pokemon += "\n";
                                curr_mons = "";
                            }
                        }
                    }
                }
            }
        }
        return pokemon;
    }

    public static void saveInput(String input) {
        String file = CURR_PATH + INPUT;
        PrintWriter writer;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch(IOException e) {
            System.out.println("ERROR: Could not write to file: " + file);
            return;
        }

        writer.write("Number,Name\n");

        String[] inputList = input.split(",");
        for(int i = 0; i < inputList.length; i++) {
            writer.write((i + 1) + ",");
            String name = inputList[i].trim().replace(" ", "-");
            writer.write(name + "\n");
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String command = "";

        label:
        while(true) {
            System.out.println("Enter command: ");

            command = scanner.nextLine();
            switch (command) {
                case "exit":
                case "quit":
                    break label;
                case "getPokemonList":
                    System.out.println("Include Forms? ");
                    boolean form = Boolean.parseBoolean(scanner.nextLine());
                    System.out.println("You chose forms: " + form);
                    getPokemonList(form);
                    break;
                case "learnsMove":
                    System.out.println("Enter move: ");
                    String move = scanner.nextLine();
                    System.out.println(learnsMove(move));
                    break;
                case "saveInput":
                    System.out.println("Enter input: ");
                    String input = scanner.nextLine();
                    saveInput(input);
                    break;
                case "getFiles":
                    System.out.println(Arrays.toString(FILES));
                    break;
                case "getCommands":
                    System.out.println(Arrays.toString(CMDS));
                    break;
                default:
                    System.out.println("ERROR: Unknown command: " + command);
                    break;
            }


        }
        scanner.close();
    }
}
