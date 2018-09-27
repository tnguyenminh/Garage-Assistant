package garage.assistant.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.*;
import garage.assistant.alert.AlertMaker;
import garage.assistant.database.DatabaseHandler;
import garage.assistant.util.GarageAssistantUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainController implements Initializable {
    @FXML
    private HBox motorbike_info;
    @FXML
    private HBox member_info;
    @FXML
    private TextField motorbikeIdInput;
    @FXML
    private Text motorbikeProducer;
    @FXML
    private Text motorbikeType;
    @FXML
    private Text motorbikeName;
    @FXML
    private Text motorbikeStatus;
    @FXML
    private JFXTextField memberIdInput;
    @FXML
    private Text memberName;
    @FXML
    private Text memberMobile;
    @FXML
    private StackPane rootPane;
    @FXML
    private JFXTextField motorID;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private Text txtMemberName;
    @FXML
    private Text txtMemberEmail;
    @FXML
    private Text txtMemberMobile;
    @FXML
    private Text txtMotorbikeProducer;
    @FXML
    private Text txtMotorbikeName;
    @FXML
    private Text txtMotorbikeType;
    @FXML
    private Text txtMotorbikeColor;
    @FXML
    private Text txtIssueDate;
    @FXML
    private Text txtIssueNoDays;
    @FXML
    private Text txtIssueFine;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private JFXButton btnRenew;
    @FXML
    private JFXButton btnSubmission;
    @FXML
    private HBox submissionDataContainer;
    @FXML
    private StackPane motorbikeInfoContainer;    
    @FXML
    private StackPane motorbikeTypeContainer;
    @FXML
    private Tab motorbikeIssueTab;
    @FXML
    private Tab renewSubmitTab;
    @FXML
    private ListView<String> lstOverdue;
    @FXML
    private Tab overdueTab;
    @FXML
    private JFXTextField noDays;
    @FXML
    private JFXTextField daysAdded;
    @FXML
    private JFXButton btnIssue;
    
    PieChart motorbikeChart;
    PieChart motorbikeTypeChart;
    Boolean isReadyForSubmission = false;
    DatabaseHandler databaseHandler;
    int fee = 0;
    int deposit = 0;
    int returnDate = 0;
    double finePerDay = 0;
    boolean isReadyForRenew = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        databaseHandler = DatabaseHandler.getInstance();
        initDrawer();
        initGraphs();
        
        try {
            initOverdues();
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void loadMotorbikeInfo(ActionEvent event) {
        //clear the pane to load infs
        clrMotorbikeCached();
        toggleGraphs(false);
        
        String id = motorbikeIdInput.getText();
        String qr = "SELECT * FROM MOTORBIKE WHERE idMotorbike = '" + id + "'";
        Boolean isExist = false;
        System.out.println(qr);//debug
        ResultSet rs = databaseHandler.excQuery(qr);

        try {
            while (rs.next()) { //set inf into textfield
                isExist = true;
                
                String mbProducer = rs.getString("producer");
                String mbName = rs.getString("name");
                int mbType = rs.getInt("type");
                int mbStatus = rs.getInt("status");

                motorbikeProducer.setText(mbProducer);
                motorbikeType.setText(GarageAssistantUtil.categorizeVehicle(mbType));//shorted
                motorbikeName.setText(mbName);
                motorbikeStatus.setText(GarageAssistantUtil.vehicleStatus(mbStatus));
            }
            if (!isExist) { //does not exist
                clrMotorbikeCached();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void loadMemberInfo(ActionEvent event) {
        //clear the pane to load infs
        clrMemberCached();
        toggleGraphs(false);
        
        String id = memberIdInput.getText().replaceAll("[^\\w\\s]","");
        String qr = "SELECT * FROM MEMBER WHERE idMember = '" + id + "'";
        Boolean flag = false;

        ResultSet rs = databaseHandler.excQuery(qr);

        try {
            while (rs.next()) { //set info to textfields
                String mbName = rs.getString("name");
                String mbMobile = rs.getString("mobile");

                memberName.setText(mbName);
                memberMobile.setText(mbMobile);

                flag = true;
            }
            if (!flag) { //doesnt exist
                clrMemberCached();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //clear old text when can not find the infs
    void clrMotorbikeCached() {
        motorbikeName.setText("");
        motorbikeProducer.setText("");
        motorbikeType.setText("");
        motorbikeStatus.setText("");
    }

    //clear old text
    void clrMemberCached() {
        memberName.setText("");
        memberMobile.setText("");
    }

    @FXML
    private void loadIssueOperation(ActionEvent event) {
        String memID = memberIdInput.getText().replaceAll("[^\\w\\s]","");
        String mtbID = motorbikeIdInput.getText().replaceAll("[^\\w\\s]","");
        int mtbStatus = 0; //for secure

        //check if motor is ready for issue operation
        String chkStt = "SELECT * FROM MOTORBIKE WHERE idMotorbike = '" + mtbID + "'";;
        ResultSet rs = databaseHandler.excQuery(chkStt);
        try {
            while (rs.next()) {
                mtbStatus = rs.getInt("status");
                fee = rs.getInt("baseFee");
                finePerDay = rs.getInt("finePercent");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if ( mtbStatus != 1) {//not avaiable
            JFXButton btt = new JFXButton("OK, Lemme check");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btt), "Failed", "This motorbike is NOT available to issue!");
            return;
        }
        
        //get days member want to rent the vehicle
        returnDate = Integer.parseInt(noDays.getText());
        
        //YES
        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            String strIssue = "INSERT INTO ISSUE(id_motorbike,id_member, expectedReturnDay, isSubmitted) VALUES ("
                    + "'" + mtbID + "',"
                    + "'" + memID + "',"
                    + "'" + returnDate + "',"
                    + "'0')";
            
            if (returnDate <=0) {
                JFXButton btt = new JFXButton("OK, Lemme check");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btt), "Failed", "Invalid Expected Return Day: " + returnDate);
                return;
            }
            System.out.println(strIssue);

            String strUpdStt = "UPDATE MOTORBIKE SET status = 0 WHERE idMotorbike = '" + mtbID + "'";

            if (databaseHandler.excAction(strIssue) && databaseHandler.excAction(strUpdStt)) {
                JFXButton button = new JFXButton("Done");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(button), "Issuing completed!", null);
                refreshGraphs();
            } else {//can not issue or update status
                JFXButton button = new JFXButton("Ok, Lemme check!");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(button), "Issue Operation FAILED", null);
            }
            clearIssueEntries();
        });
        
        //NO
        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            JFXButton button = new JFXButton("Ok");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(button), "Issue Cancelled", null);
            
            clearIssueEntries();
        });
        
        deposit = fee * returnDate;
        finePerDay = fee * finePerDay / 100;
        
        //confirm
        AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(yesButton, noButton), "Confirm" , "Vehicle: " + motorbikeName.getText()
                + "\nMember: " + memberName.getText() + "\nDeposit: $" + deposit + "\nFine/day: $" + finePerDay);
    }

    @FXML
    private void loadIssueInfo(ActionEvent event) {
        clearEntries();
        isReadyForSubmission = false;

        try{
            String id = motorID.getText().replaceAll("[^\\w\\s]",""); //avoid SQLi
            //use a single query for better performance
            String qr = "SELECT ISSUE.id_motorbike, ISSUE.id_member, ISSUE.issueTime, ISSUE.expectedReturnDay, \n" +
                        "MEMBER.name AS mbName, MEMBER.mobile, MEMBER.email,\n" +
                        "MOTORBIKE.producer, MOTORBIKE.name AS mtName, MOTORBIKE.type, MOTORBIKE.color, MOTORBIKE.baseFee, MOTORBIKE.finePercent\n" +
                        "FROM ISSUE\n" +
                        "LEFT JOIN MEMBER\n" +
                        "ON ISSUE.id_member = MEMBER.idMember\n" +
                        "LEFT JOIN MOTORBIKE\n" +
                        "ON ISSUE.id_motorbike = MOTORBIKE.idMotorbike\n" +
                        "WHERE ISSUE.id_motorbike = '" + id + "' AND isSubmitted = 0";
            ResultSet rs = databaseHandler.excQuery(qr);
            System.out.println(qr);
            if(rs.next()) {//exist
                //member inf
                txtMemberName.setText(rs.getString("mbName"));
                txtMemberEmail.setText(rs.getString("email"));
                txtMemberMobile.setText(rs.getString("mobile"));
                
                //motorbike inf
                txtMotorbikeProducer.setText(rs.getString("producer"));
                txtMotorbikeName.setText(rs.getString("mtName"));
                txtMotorbikeType.setText(GarageAssistantUtil.categorizeVehicle(rs.getInt("type")));//shorted
                txtMotorbikeColor.setText(rs.getString("color"));
                
                int expectedReturnDay = rs.getInt("expectedReturnDay");
                int fee = rs.getInt("baseFee");
                int finePer = rs.getInt("finePercent");
                
                //issue inf
                Timestamp issueTime = rs.getTimestamp("issueTime");
                Date dateOfIssue = new Date(issueTime.getTime());
                txtIssueDate.setText(dateOfIssue.toString());
                Long timeElapsed = System.currentTimeMillis() - issueTime.getTime();
                Long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.MILLISECONDS) + 1;
                String daysElapsed = String.format("Used for %d day(s)", days);//used days
                txtIssueNoDays.setText(daysElapsed);
                double fine = GarageAssistantUtil.getFineAmount(days.intValue(), expectedReturnDay, fee, finePer);
                if (fine > 0) {
                    DecimalFormat currencyFormatter = new DecimalFormat("####,###,###.#"); //formatting
                    txtIssueFine.setText("Fine: $" + currencyFormatter.format(fine));
                    txtIssueFine.setFill(Color.web("#E452E4")); //easier to see
                } else {
                    int remainDays = expectedReturnDay - days.intValue();
                    txtIssueFine.setText(remainDays + " day(s) remaning!"); //allowed days
                }
                isReadyForSubmission = true; //everything is set
                toggleControls(true); //enable controls
                submissionDataContainer.setOpacity(1); //unhide
            } else { //does not exist
                JFXButton button = new JFXButton("Lemme try again!");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(button), "No such Motor exists in Issue database", null);
            }
        } catch(SQLException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void loadSubmissionOperation(ActionEvent event) {
        if (!isReadyForSubmission) { //not ready 
            JFXButton btn = new JFXButton("Lemme check again!");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn), "Failed!", "Invalid Motorbike to submit.");
            return;
        }

        //confirm button
        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            String id = motorID.getText().replaceAll("[^\\w\\s]","");
            //1. remove the entry from the Issue table
            String actDel = "UPDATE ISSUE SET isSubmitted = 1 WHERE id_motorbike = '" + id + "' AND isSubmitted = 0";
            //2. make the motor available in the database
            String actUpd = "UPDATE MOTORBIKE SET status = 1 WHERE idMotorbike = '" + id + "'";
            if (databaseHandler.excAction(actDel) && databaseHandler.excAction(actUpd)) {//success
                JFXButton btn = new JFXButton("OK");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn),"Success!", "Motorbike has been submitted.");
                motorID.clear();//refresh
                loadIssueInfo(null); 
            } else {//error
                JFXButton btn = new JFXButton("Lemme check again");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn),"Failed!", "Submission has been failed.");
            }
        });
        
        //cancel button
        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            JFXButton btn = new JFXButton("Sure");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn), "Cancelled", "Submission Operation cancelled!");
        });
        
        AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(yesButton, noButton), "Confirm", "Are you sure want to return the motorbike?");
    }
    
    //renew Expected Return Day
    @FXML
    private void loadRenewOperation(ActionEvent event) {
        if (!isReadyForSubmission) { //not ready 
            JFXButton btn = new JFXButton("Let me check again");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane,Arrays.asList(btn), "Failed!", "Invalid Motorbike to renew.");
            return;
        }

        String id = motorID.getText().replaceAll("[^\\w\\s]","");
        int added = Integer.parseInt(daysAdded.getText());
        
        String chkStt = "SELECT * FROM MOTORBIKE WHERE idMotorbike = '" + id + "'";;
        ResultSet rs = databaseHandler.excQuery(chkStt);
        try {
            while (rs.next()) {
                fee = rs.getInt("baseFee");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (added <= 0) {//invalid day
            JFXButton btn = new JFXButton("Let me check again");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane,Arrays.asList(btn), "Failed!", "Invalid Day");
            return;
        }

        //confirm
        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            String actUpd = "UPDATE ISSUE SET expectedReturnDay = expectedReturnDay + " + added + " WHERE id_motorbike = '" + id + "'";
            
            if (databaseHandler.excAction(actUpd)) {//success
                JFXButton btn = new JFXButton("OK");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn), "Success!", "Motorbike has been renewed.\n" + added + " day(s) added!");
                loadIssueInfo(null); //refresh
                System.out.println(actUpd);
                daysAdded.clear();
            } else {//error
                JFXButton btn = new JFXButton("Lemme check again");
                AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn),"Failed!", "Renewal has been failed.");
            }
        });
        
        //cancel
        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1)->{
            JFXButton btn = new JFXButton("SURE");
            AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(btn),"Cancelled", "Renew Operation cancelled!");
        });
        
        int addedDep = added * fee;
        
        //show alert
        AlertMaker.showMaterialDialog(rootPane, rootBorderPane, Arrays.asList(yesButton, noButton), "Confirm", "Are you sure want to renew the motorbike?\nFee: $" + addedDep);
    }

    @FXML
    private void handleMenuClose(ActionEvent event) {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    @FXML
    private void handleMenuAddMember(ActionEvent event) {
        GarageAssistantUtil.loadWindow(getClass().getResource("/garage/assistant/ui/addmember/member_add.fxml"), "Add new Member", null);
    }

    @FXML
    private void handleMenuAddMotorbike(ActionEvent event) {
        GarageAssistantUtil.loadWindow(getClass().getResource("/garage/assistant/ui/addmotorbike/add_motorbike.fxml"), "Add new Vehicle", null);
    }

    @FXML
    private void handleMenuViewMembers(ActionEvent event) {
        GarageAssistantUtil.loadWindow(getClass().getResource("/garage/assistant/ui/listmember/member_list.fxml"), "All Member", null);
    }

    @FXML
    private void handleMenuViewMotorbikes(ActionEvent event) {
        GarageAssistantUtil.loadWindow(getClass().getResource("/garage/assistant/ui/listmotorbike/motorbike_list.fxml"), "All Vehicle", null);
    }

    @FXML
    private void handleMenuFullScreen(ActionEvent event) {
        Stage stage = ((Stage) rootPane.getScene().getWindow());
        stage.setFullScreen(!stage.isFullScreen());//toggle full screen & windowed
    }
    
    @FXML
    private void handleMenuAbout(ActionEvent event) {
        GarageAssistantUtil.loadWindow(getClass().getResource("/garage/assistant/ui/about/about.fxml"), "About", null);
    }
    
    @FXML
    private void handleMenuAnalyseVehicleOfMonth(ActionEvent event) {
    }

//    private void initDrawer() {
//        try {
//            VBox toolbar = FXMLLoader.load(getClass().getResource("/garage/assistant/ui/main/toolbar/toolbar.fxml"));
//            drawer.setSidePane(toolbar); //call VBox toolbar
//        } catch (IOException ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        HamburgerSlideCloseTransition task =  new HamburgerSlideCloseTransition(hamburger);
//        task.setRate(-1); //use for toggle icon
//        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
//            task.setRate(task.getRate() * -1); //toggle icon
//            task.play(); //call the toolbar
//            
//            if(drawer.isClosed()) {
//                drawer.open();
//            } else {
//                drawer.close();
//            }
//        });
//    }
    
    private void initDrawer() {
        try {
            VBox toolbar = FXMLLoader.load(getClass().getResource("/garage/assistant/ui/main/toolbar/toolbar.fxml"));
            drawer.setSidePane(toolbar); //call VBox toolbar
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        HamburgerSlideCloseTransition task = new HamburgerSlideCloseTransition(hamburger);
        task.setRate(-1); //for toggling icon
        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
            drawer.toggle(); //slide
        });
        drawer.setOnDrawerOpening((event) -> {
            task.setRate(task.getRate() * -1);
            task.play();
            drawer.toFront();
        });
        drawer.setOnDrawerClosed((event) -> {
            drawer.toBack();
            task.setRate(task.getRate() * -1);
            task.play();
        });
    }

    private void clearEntries() {
        //member inf
        txtMemberName.setText("");
        txtMemberEmail.setText("");
        txtMemberMobile.setText("");

        //motorbike inf
        txtMotorbikeProducer.setText("");
        txtMotorbikeName.setText("");
        txtMotorbikeType.setText("");
        txtMotorbikeColor.setText("");

        //issue inf
        txtIssueDate.setText("");
        txtIssueNoDays.setText("");
        txtIssueFine.setText("");
        
        toggleControls(false); //hide controls
        submissionDataContainer.setOpacity(0); //hide it
    }
    
    private void toggleControls(Boolean enableFlag) { //control buttons
        if (enableFlag) {
            btnSubmission.setDisable(false);
        } else {
            btnRenew.setDisable(true);
            btnSubmission.setDisable(true);
            btnIssue.setDisable(true);
        }
    }

    private void clearIssueEntries() { //remove texts from the UI
        motorbikeIdInput.clear();
        memberIdInput.clear();
        noDays.clear();
        
        motorbikeType.setText("");
        motorbikeProducer.setText("");
        motorbikeName.setText("");
        motorbikeStatus.setText("");
        
        memberName.setText("");
        memberMobile.setText("");
        
        toggleGraphs(true);
    }

    private void initGraphs() {
        motorbikeChart = new PieChart(databaseHandler.getMotorbikeStatistics());
        motorbikeTypeChart = new PieChart(databaseHandler.getMotorbikeTypes());
        
        motorbikeInfoContainer.getChildren().add(motorbikeChart);
        motorbikeTypeContainer.getChildren().add(motorbikeTypeChart);
                
        motorbikeIssueTab.setOnSelectionChanged((Event event) -> {//refresh whenever change the selection
            clearIssueEntries();
            if (motorbikeIssueTab.isSelected()) {
                refreshGraphs();
            } else {
                motorID.clear();
                clearEntries();
                daysAdded.clear();
                
                try {//overdue tab
                    initOverdues();
                } catch (SQLException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    private void refreshGraphs() {
        motorbikeChart.setData(databaseHandler.getMotorbikeStatistics());
        motorbikeTypeChart.setData(databaseHandler.getMotorbikeTypes());
    }
    
    private void toggleGraphs(Boolean status) {
        if (status) { //visible
            motorbikeChart.setOpacity(1);
            motorbikeTypeChart.setOpacity(1);
        } else { //hide
            motorbikeChart.setOpacity(0);
            motorbikeTypeChart.setOpacity(0);
        }
    }

    private void initOverdues() throws SQLException {
        ObservableList<String> overdues = FXCollections.observableArrayList();
        int noOverdueVehicles = 0;
        
        String qr = "SELECT ISSUE.id_motorbike, ISSUE.id_member, ISSUE.issueTime, ISSUE.expectedReturnDay, \n" +
                    "MEMBER.name AS mbName, MEMBER.mobile, MEMBER.email,\n" +
                    "MOTORBIKE.producer, MOTORBIKE.name AS mtName, MOTORBIKE.type, MOTORBIKE.color, MOTORBIKE.status, MOTORBIKE.baseFee, MOTORBIKE.finePercent\n" +
                    "FROM ISSUE\n" +
                    "LEFT JOIN MEMBER\n" +
                    "ON ISSUE.id_member = MEMBER.idMember\n" +
                    "LEFT JOIN MOTORBIKE\n" +
                    "ON ISSUE.id_motorbike = MOTORBIKE.idMotorbike\n"+
                    "WHERE ISSUE.isSubmitted = '0'";
        
        ResultSet rs = databaseHandler.excQuery(qr);
        
        try {
            while(rs.next()) {
                
                if (rs.getInt("status") == 0) {
                    int expectedReturnDay = rs.getInt("expectedReturnDay");
                    int fee = rs.getInt("baseFee");
                    int finePer = rs.getInt("finePercent");
                    
                    //calculate fine amount
                    Timestamp issueTime = rs.getTimestamp("issueTime");
                    Date dateOfIssue = new Date(issueTime.getTime());
                    txtIssueDate.setText(dateOfIssue.toString());
                    Long timeElapsed = System.currentTimeMillis() - issueTime.getTime();
                    Long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.MILLISECONDS) + 1;//used days
                    int fineDays = days.intValue() - expectedReturnDay;
                    double fine = GarageAssistantUtil.getFineAmount(days.intValue(), expectedReturnDay, fee, finePer);
                    if (fine > 0) {
                        noOverdueVehicles++; //inscree the number of overdued Vehicles
                        //motorbike inf
                        String mtbId = rs.getString("id_motorbike");
                        String mtbProc = rs.getString("producer");
                        String mtbName = rs.getString("mtName");
                        String mtbColor = rs.getString("color");
                        
                        //member inf
                        String mbrId = rs.getString("id_member");
                        String mbrName = rs.getString("mbName");
                        String mbrMobile = rs.getString("mobile");
                        String mbrEmail = rs.getString("email");
                        DecimalFormat currencyFormatter = new DecimalFormat("####,###,###.#"); //formatting

                        overdues.add("");
                        overdues.add("[" + noOverdueVehicles + "] "+ mtbId + " | " + mtbProc + ", " + mtbName + " (" + mtbColor + ")");
                        overdues.add("\tFINE: $" + currencyFormatter.format(fine) + " | " + days + " day(s) issued in total:" + " | " + fineDays + " fine day(s)");

                        overdues.add("Member:");
                        overdues.add("\t" + mbrName + " | ID: " + mbrId);
                        overdues.add("\tMobile: " + mbrMobile + " | Email: " + mbrEmail);
                        
                    }
                }
            }
            lstOverdue.getItems().setAll(overdues);
            if (noOverdueVehicles > 0)
                overdueTab.setText("Overdue(" + noOverdueVehicles + ")");//show the number of overdue vehicle in tabpane
            else
                overdueTab.setText("Overdue");
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void enableRenewBtn(ActionEvent event) {
        btnRenew.setDisable(false);
    }

    @FXML
    private void enableIssueBtn(ActionEvent event) {
        btnIssue.setDisable(false);
    }
    
    

}