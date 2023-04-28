import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacmanPackageSearch {
    /*
     * The main class
     */
    public static void main(String[] args) throws Exception {
        promptAll();
    }



    /*
     * Collect options and open rofi
     */
    public static void promptAll() throws Exception {
        List<String> options = new ArrayList<>();
        options.addAll(Arrays.asList(getPacmanAvailablePackages()));
        options.addAll(Arrays.asList(getPacmanAvailableGroups(true)));
        options.sort(Collator.getInstance());

        String[] finalOptions = options.toArray(new String[0]);

        String selected = prompt(finalOptions, "Choose a package or a group");
        if (selected == null) return;

        String[] selectedParts = selected.split(" ");
        boolean group = selectedParts[1].equals("(group)");

        if (group) {
            openGroupPage(selectedParts[0]);
        } else {
            openPackagePage(selectedParts[1].replaceAll("[()]", ""), selectedParts[0]);
        }
    }



    /*
     * Basic rofi prompt
     */
    public static String prompt(String[] options, String prompt) throws Exception {
        String[] output = getOutputPipe(String.join("\n", options),
                "rofi", "-dmenu", "-i", "-p", prompt, "-l", "20");

        if (output.length == 0) return null;
        return output[0];
    }



    /*
     * Utils for opening package/group info page
     */
    public static void openPackagePage(String repo, String pkg) throws Exception {
        Desktop.getDesktop().browse(new URI(String.format("https://archlinux.org/packages/%s/x86_64/%s", repo, pkg)));
    }

    public static void openGroupPage(String group) throws Exception {
        Desktop.getDesktop().browse(new URI(String.format("https://archlinux.org/groups/x86_64/%s", group)));
    }



    /*
     * Pacman Utilities
     */
    public static String[] getPacmanAvailablePackages() throws Exception {
        String[] output = getOutput("pacman", "-Ss");

        List<String> pkg_sort = new ArrayList<>(); // First this, then we sort

        for (String l : output) {
            if (l.startsWith(" ")) continue; // Filter the descriptions, since they are printed with insets

            String[] parts = l.split(" "); // Split at the space, so we can get the repo/pkg and the version
            String[] pkg_parts = parts[0].split("/"); // Finally we get the repo and package array

            String repo = pkg_parts[0];
            String package_name = pkg_parts[1];
            String version = parts[1];

            pkg_sort.add(package_name+ " (" + repo + ") " + version);
        }

        pkg_sort.sort(Collator.getInstance()); // Sort the packages
        return pkg_sort.toArray(new String[0]);
    }

    public static String[] getPacmanAvailableGroups(boolean addTag) throws Exception {
        String[] output = getOutput("pacman", "-Sg"); // Simply get the pacman query
        List<String> groups = new ArrayList<>();
        String suffix = addTag ? " (group)" : "";

        for (String l : output) {
            groups.add(l + suffix); // Add the group tag if searching all
        }

        groups.sort(Collator.getInstance()); // Sort

        return groups.toArray(new String[0]);
    }



    /*
     * Command execution utilities
     */
    public static String[] getOutput(String... command) throws Exception {
        // Run command
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);

        // Get the STDOUT reader
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String s;

        // Iterate through the output lines
        List<String> lines = new ArrayList<>();
        while ((s = stdInput.readLine()) != null) {
            lines.add(s);
        }

        // Return lines as array
        return lines.toArray(new String[0]);
    }

    public static String[] getOutputPipe(String pipe, String... command) throws Exception {
        // Run command
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);

        // Try to pipe into the program
        process.outputWriter().write(pipe);

        // Get the STDOUT reader
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String s;

        // Iterate through the output lines
        List<String> lines = new ArrayList<>();
        while ((s = stdInput.readLine()) != null) {
            lines.add(s);
        }

        // Return lines as array
        return lines.toArray(new String[0]);
    }
}
