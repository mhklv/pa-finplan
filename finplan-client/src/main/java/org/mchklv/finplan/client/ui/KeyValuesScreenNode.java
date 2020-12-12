package org.mchklv.finplan.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mchklv.finplan.client.KeyValueGroupVariant;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class KeyValuesScreenNode extends SplitPane implements GroupChosenRightNode.KeyValueAddable,
                                                   ValueChosenRightNode.ChosenKeyValueContainer {
    @FXML
    private TreeView<KeyValueGroupVariant> keyValuesTreeView;
    @FXML
    private VBox rightPane;
    // private ScrollPane rightScrollPane;

    private DataProvider dataProvider;
    private NavigatableScene parentScene;

    public KeyValuesScreenNode(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        UICustomUtils.loadControllerFXML(this, "fxml/keyValuesScreen.fxml");
        initKeyValuesTree();
        setDividerPositions(0.35f, 0.65f);
    }
    
    public void deleteKeyValue(KeyValue keyValue) {
        TreeItem<KeyValueGroupVariant> treeItem = locateKeyValueinTree(keyValue);
        if (treeItem == null) {
            return;
        }
        
        try {
            dataProvider.deleteKeyValue(keyValue);
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке удалить ключевую ценность.");
            return;
        }
        treeItem.getParent().getChildren().remove(treeItem);
    }
    
    // public void createNewKeyValue(KeyValueGroup parentGroup) {
    //     // KeyValue newKeyValue = new KeyValue(null, "Новая ключевая ценность", "");
    //     addKeyValue(parentGroup, newKeyValue);
    // }

    public void addKeyValue(KeyValueGroup parentGroup, KeyValue newValue) {
        if (newValue.getId() == null) {
            return;
        }
        
        TreeItem<KeyValueGroupVariant> parentTreeItem = getTreeItemByGroup(parentGroup);
        
        if (parentTreeItem == null) {
            return;
        }

        List<TreeItem<KeyValueGroupVariant>> valueItemsInGroup = parentTreeItem.getChildren();

        for (TreeItem<KeyValueGroupVariant> valueItem : valueItemsInGroup) {
            if (valueItem.getValue().getKeyValue().getId() == newValue.getId()) {
                return;
            }
        }

        KeyValueGroupVariant newKeyValueVariant = new KeyValueGroupVariant(newValue);
        TreeItem<KeyValueGroupVariant> newTreeItem = new TreeItem<KeyValueGroupVariant>(newKeyValueVariant);
        parentTreeItem.getChildren().add(newTreeItem);
    }

    public void setAsRoot(NavigatableScene parentScene) {
        this.parentScene = parentScene;
        parentScene.setNewRoot(this);
    }


    private TreeItem<KeyValueGroupVariant> getTreeItemByGroup(KeyValueGroup targetGroup) {
        List<TreeItem<KeyValueGroupVariant>> groupItems = keyValuesTreeView.getRoot().getChildren();

        for (TreeItem<KeyValueGroupVariant> groupItem : groupItems) {
            if (groupItem.getValue().getKeyValueGroup().getId() == targetGroup.getId()) {
                return groupItem;
            }
        }

        return null;
    }

    private void initKeyValuesTree() {
        // keyValuesTreeView.getSelectionModel()
        keyValuesTreeView.setCellFactory(new Callback<TreeView<KeyValueGroupVariant>,TreeCell<KeyValueGroupVariant>>(){

                @Override
                public TreeCell<KeyValueGroupVariant> call(TreeView<KeyValueGroupVariant> treeView) {
                    return new TreeCell<KeyValueGroupVariant>() {

						@Override
						protected void updateItem(KeyValueGroupVariant item, boolean empty) {
							super.updateItem(item, empty);

                            if (empty || item == null || item.isEmpty()) {
                                this.setText(null);
                                this.setGraphic(null);
                            }
                            else {
                                if (item.isKeyValue()) {
                                    this.setText(item.getKeyValue().getName());
                                }
                                else {
                                    this.setText(item.getKeyValueGroup().getName());
                                }
                            }
						}
                    };
                }
            });
        keyValuesTreeView.setRoot(new TreeItem<KeyValueGroupVariant>());
        keyValuesTreeView.setShowRoot(false);

        initTreeGroups();
        initTreeValues();
        initTreeGroupsSelection();
        keyValuesTreeView.getSelectionModel().selectFirst();
    }

    private void initTreeGroupsSelection() {
        keyValuesTreeView.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<TreeItem<KeyValueGroupVariant>>() {

                @Override
                public void changed(ObservableValue<? extends TreeItem<KeyValueGroupVariant>> observable,
                                    TreeItem<KeyValueGroupVariant> oldValue, TreeItem<KeyValueGroupVariant> newValue) {
                    
                    KeyValueGroupVariant chosenVariant = newValue.getValue();

                    if (chosenVariant.isKeyValueGroup()) {
                        GroupChosenRightNode newRightNode = new GroupChosenRightNode(dataProvider,
                                        chosenVariant.getKeyValueGroup(), KeyValuesScreenNode.this);
                        rightPane.getChildren().clear();
                        rightPane.getChildren().add(newRightNode);
                        // rightScrollPane.setContent(newRightNode);
                        // rightPanelGroup.getChildren().clear();
                        // rightPanelGroup.getChildren().add(newRightNode);
                    }
                    else if (chosenVariant.isKeyValue()) {
                        KeyValueGroup parentGroup = newValue.getParent().getValue().getKeyValueGroup();
                        ValueChosenRightNode newRightNode = new ValueChosenRightNode(dataProvider, chosenVariant.getKeyValue(),
                                                                                     parentGroup, KeyValuesScreenNode.this);
                        newRightNode.prefHeightProperty().bind(getScene().heightProperty());
                        TaskEventsScreen tasksScreenNode = new TaskEventsScreen(dataProvider,
                                                                                chosenVariant.getKeyValue());
                        TasksActivitiesTabPane newTabPane = new TasksActivitiesTabPane();
                        newTabPane.setRationaleNode(newRightNode);
                        newTabPane.setTasksScreenNode(tasksScreenNode);
                        rightPane.getChildren().clear();
                        rightPane.getChildren().add(newTabPane);
                        // rightScrollPane.setContent(newRightNode);
                        // rightRegion.setRationaleNode(newRightNode);
                    }
                    else {
                        // Unknown error
                    }
                }
            });
    }

    private LinkedList<KeyValueGroup> retrieveGroups() {
        LinkedList<KeyValueGroup> resList;
            
        try {
            resList = dataProvider.getAllKeyValueGroups();
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                    "Проиошла ошибка при соединении с сервером при попытке загрузить список групп ключевых ценностей.");
            return new LinkedList<KeyValueGroup>();
        }

        return resList;
    }

    private LinkedList<KeyValue> retrieveValues(KeyValueGroup parentGroup) {
        LinkedList<KeyValue> resList;

        try {
            resList = dataProvider.getAllKeyValues(parentGroup);
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                    "Проиошла ошибка при соединении с сервером при попытке загрузить ключевые ценности.");
            return new LinkedList<KeyValue>();
        }

        return resList;
    }

    private void initTreeGroups() {
        Map<String, Integer> groupsOrderMap = new HashMap<String, Integer>();
        groupsOrderMap.put("Эталонные экосистемы", 1);
        groupsOrderMap.put("Значимые экосистемы (редкие, исчезающие, эндемичные, эколого-стабилизирующие функции)", 2);
        groupsOrderMap.put("Редкие виды флоры (Красная книга РК, МСОП)", 3);
        groupsOrderMap.put("Ценные пищевые, лекарственные растения", 4);
        groupsOrderMap.put("Редкие виды фауны (Красная книга РК, МСОП)", 5);
        groupsOrderMap.put("Охотничье-промысловые виды", 6);
        groupsOrderMap.put("Ключевые охраняемые виды и крупные скопления животных", 7);
        groupsOrderMap.put("Экосистемные услуги", 8);
        groupsOrderMap.put("Генетические ресурсы", 9);
        groupsOrderMap.put("Уникальные природные комплексы и объекты", 10);
        groupsOrderMap.put("Глобально значимые природные комплексы и объекты UNESCO, IBA, KBA, Ramsar", 11);
        groupsOrderMap.put("Памятники истории и культуры", 12);
        groupsOrderMap.put("Рекреационные и бальнеологические ресурсы", 13);

        List<KeyValueGroup> groups = retrieveGroups();
        Collections.sort(groups, new Comparator<KeyValueGroup>() {
                
                @Override
                public int compare(KeyValueGroup o1, KeyValueGroup o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    
                    Integer o1Place = groupsOrderMap.get(o1.getName());
                    Integer o2Place = groupsOrderMap.get(o2.getName());

                    if (o1Place == null || o2Place == null) {
                        return 0;
                    }

                    return Integer.compare(o1Place, o2Place);
                }
            });
        
        for (KeyValueGroup group : groups) {
            KeyValueGroupVariant groupVariant = new KeyValueGroupVariant(group);
            TreeItem<KeyValueGroupVariant> groupTreeItem = new TreeItem<>(groupVariant);
            keyValuesTreeView.getRoot().getChildren().add(groupTreeItem);
        }
    }

    private void initTreeValues() {
        List<TreeItem<KeyValueGroupVariant>> groupItems = keyValuesTreeView.getRoot().getChildren();

        for (TreeItem<KeyValueGroupVariant> groupItem : groupItems) {
            LinkedList<KeyValue> keyValues = retrieveValues(groupItem.getValue().getKeyValueGroup());
            for (KeyValue keyValue : keyValues) {
                KeyValueGroupVariant keyValueVariant = new KeyValueGroupVariant(keyValue);
                TreeItem<KeyValueGroupVariant> valueTreeItem = new TreeItem<>(keyValueVariant);
                groupItem.getChildren().add(valueTreeItem);
            }
        }
    }

    private TreeItem<KeyValueGroupVariant> locateKeyValueinTree(KeyValue keyValue) {
        for (var groupItem : keyValuesTreeView.getRoot().getChildren()) {
            for (var keyValueItem : groupItem.getChildren()) {
                KeyValueGroupVariant variant = keyValueItem.getValue();
                if (variant.isKeyValue() && variant.getKeyValue().getId() != null &&
                    variant.getKeyValue().getId().equals(keyValue.getId())) {
                    return keyValueItem;
                }
            }
        }

        return null;
    }

	@Override
	public void refreshTreeView() {
        keyValuesTreeView.refresh();
	}
}
