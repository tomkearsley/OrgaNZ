package seng302.Commands.Modify;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;

import seng302.Actions.Action;
import seng302.Actions.ActionInvoker;
import seng302.Actions.Donor.CreateDonorAction;
import seng302.Donor;
import seng302.HistoryItem;
import seng302.State.DonorManager;
import seng302.State.State;
import seng302.Utilities.JSONConverter;
import seng302.Utilities.TypeConverters.LocalDateConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command line to create a Donor with basic information, including their DOB and full name.
 * @author Dylan Carlyle, Jack Steel
 * @version sprint 1.
 * date 05/03/2018
 */

@Command(name = "createuser", description = "Creates a user.")
public class CreateDonor implements Runnable {

    private DonorManager manager;
    private ActionInvoker invoker;

    public CreateDonor() {
        manager = State.getDonorManager();
        invoker = State.getInvoker();
    }

    public CreateDonor(DonorManager manager, ActionInvoker invoker) {
        this.manager = manager;
        this.invoker = invoker;
    }

    @Option(names = {"-f", "--firstname"}, description = "First name.", required = true)
    private String firstName;

    @Option(names = {"-l", "--lastname"}, description = "Last name.", required = true)
    private String lastName;

    @Option(names = {"-d",
            "--dob"}, description = "Date of birth.", required = true, converter = LocalDateConverter.class)
    private LocalDate dateOfBirth;

    @Option(names = {"-m", "--middlenames", "--middlename"}, description = "Middle name(s)")
    private String middleNames;

    @Option(names = "--force", description = "Force even if a duplicate user is found")
    private boolean force;

    public void run() {
        if (!force && manager.collisionExists(firstName, lastName, dateOfBirth)) {
            System.out.println("Duplicate user found, use --force to create anyway");
            return;
        }
        int uid = manager.getUid();

        Donor donor = new Donor(firstName, middleNames, lastName, dateOfBirth, uid);

        Action action = new CreateDonorAction(donor, manager);

        invoker.execute(action);

        System.out.println(String.format("New donor %s %s %s created with userID %s", firstName,
                ofNullable(middleNames).orElse(""), lastName, uid));
        HistoryItem create = new HistoryItem("CREATE", "Donor profile ID: " + uid + " created.");
        JSONConverter.updateHistory(create, "action_history.json");
    }
}