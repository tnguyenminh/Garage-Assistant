package garage.assistant.ui.listmember;

import garage.assistant.database.DatabaseHandler;
import garage.assistant.ui.listmotorbike.MotorbikeListController;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class MemberListController implements Initializable {

    ObservableList<Member> list = FXCollections.observableArrayList();

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TableView<Member> tblView;
    @FXML
    private TableColumn<Member, String> idCol;
    @FXML
    private TableColumn<Member, String> nameCol;
    @FXML
    private TableColumn<Member, String> mobileCol;
    @FXML
    private TableColumn<Member, String> emailCol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        
        loadData();
    }
    
    private void initCol() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
    }
    
    private void loadData() {
        DatabaseHandler handler = new DatabaseHandler();
        
        String qu = "SELECT * FROM MEMBER";
        ResultSet rs = handler.excQuery(qu);
        
        try {
            while(rs.next()) {
                String idx = rs.getString("idMember");
                String namex = rs.getString("name");
                String mobilex = rs.getString("mobile");
                String emailx = rs.getString("email");
                
                list.add(new Member(idx, namex, mobilex, emailx));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MotorbikeListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        tblView.getItems().setAll(list); 
    }

    public static class Member {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty mobile;
        private final SimpleStringProperty email;
        
        //constructor
        Member(String id, String name, String mobile, String email) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.mobile = new SimpleStringProperty(mobile);
            this.email = new SimpleStringProperty(email);
        }

        public String getId() {
            return id.get();
        }
        
        public String getName() {
            return name.get();
        }

        public String getMobile() {
            return mobile.get();
        }

        public String getEmail() {
            return email.get();
        }
    }    
    
}
