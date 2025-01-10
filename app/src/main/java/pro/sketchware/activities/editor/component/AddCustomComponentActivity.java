package pro.sketchware.activities.editor.component;

import static pro.sketchware.utility.GsonUtils.getGson;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;

import a.a.a.aB;
import a.a.a.wq;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.IconSelectorDialog;
import mod.hilal.saif.components.ComponentsHandler;
import mod.jbk.util.OldResourceIdMapper;

import pro.sketchware.R;
import pro.sketchware.databinding.ManageCustomComponentAddBinding;
import pro.sketchware.tools.ComponentHelper;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.FileUtil;

public class AddCustomComponentActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private boolean isEditMode = false;
    private int position = 0;

    private final String path = wq.getCustomComponent();

    private ManageCustomComponentAddBinding binding;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(_savedInstanceState);
        binding = ManageCustomComponentAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handleInsetts(binding.getRoot());
        init();
    }

    private void init() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        binding.btnCancel.setOnClickListener(Helper.getBackPressedClickListener(this));
        if (getIntent().hasExtra("pos")) {
            isEditMode = true;
            position = getIntent().getIntExtra("pos", 0);
        }
        getViewsById();
        if (isEditMode) {
            setTitle(Helper.getResString(R.string.event_title_edit_component));
            fillUp();
        } else {
            setTitle(Helper.getResString(R.string.event_title_add_new_component));
            initializeHelper();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Import");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            showFilePickerDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillUp() {
        if (FileUtil.isExistFile(path)) {
            ArrayList<HashMap<String, Object>> list = getGson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
            HashMap<String, Object> map = list.get(position);
            setupViews(map);
        }
    }

    private void getViewsById() {
        binding.btnSave.setOnClickListener(this);
        binding.pick.setOnClickListener(this);
    }

    private void initializeHelper() {
        binding.componentName.addTextChangedListener(new ComponentHelper(new EditText[]{binding.componentBuildClass, binding.componentVarTypeName, binding.componentTypeName, binding.componentTypeClass}, binding.componentTypeClass));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_save) {
            if (!isImportantFieldsEmpty()) {
                if (OldResourceIdMapper.isValidIconId(binding.componentIcon.getText().toString())) {
                    save();
                } else {
                    SketchwareUtil.toastError(Helper.getResString(R.string.invalid_icon_id));
                    binding.componentIcon.requestFocus();
                }
            } else {
                SketchwareUtil.toastError(Helper.getResString(R.string.invalid_required_fields));
            }
        } else if (id == R.id.pick) {
            showIconSelectorDialog();
        }
    }

    private void setupViews(HashMap<String, Object> map) {
        binding.componentName.setText((String) map.get("name"));
        binding.componentId.setText((String) map.get("id"));
        binding.componentIcon.setText((String) map.get("icon"));
        binding.componentVarTypeName.setText((String) map.get("varName"));
        binding.componentTypeName.setText((String) map.get("typeName"));
        binding.componentBuildClass.setText((String) map.get("class"));
        binding.componentTypeClass.setText((String) map.get("buildClass"));
        binding.componentDescription.setText((String) map.get("description"));
        binding.componentDocUrl.setText((String) map.get("url"));
        binding.componentAddVar.setText((String) map.get("additionalVar"));
        binding.componentDefAddVar.setText((String) map.get("defineAdditionalVar"));
        binding.componentImports.setText((String) map.get("imports"));
    }

    private void showIconSelectorDialog() {
        new IconSelectorDialog(this, binding.componentIcon).show();
    }

    private boolean isImportantFieldsEmpty() {
        return binding.componentName.getText().toString().isEmpty()
                || binding.componentId.getText().toString().isEmpty()
                || binding.componentIcon.getText().toString().isEmpty()
                || binding.componentTypeName.getText().toString().isEmpty()
                || binding.componentVarTypeName.getText().toString().isEmpty()
                || binding.componentTypeClass.getText().toString().isEmpty()
                || binding.componentBuildClass.getText().toString().isEmpty();
    }

    private void save() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (FileUtil.isExistFile(path)) {
            list = getGson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
        }
        HashMap<String, Object> map = new HashMap<>();
        if (isEditMode) {
            map = list.get(position);
        }
        map.put("name", binding.componentName.getText().toString());
        map.put("id", binding.componentId.getText().toString());
        map.put("icon", binding.componentIcon.getText().toString());
        map.put("varName", binding.componentVarTypeName.getText().toString());
        map.put("typeName", binding.componentTypeName.getText().toString());
        map.put("buildClass", binding.componentBuildClass.getText().toString());
        map.put("class", binding.componentTypeClass.getText().toString());
        map.put("description", binding.componentDescription.getText().toString());
        map.put("url", binding.componentDocUrl.getText().toString());
        map.put("additionalVar", binding.componentAddVar.getText().toString());
        map.put("defineAdditionalVar", binding.componentDefAddVar.getText().toString());
        map.put("imports", binding.componentImports.getText().toString());
        if (!isEditMode) {
            list.add(map);
        }
        FileUtil.writeFile(path, getGson().toJson(list));
        SketchwareUtil.toast(Helper.getResString(R.string.common_word_saved));
        finish();
    }

    private void showFilePickerDialog() {
        DialogProperties properties = new DialogProperties();

        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = new String[]{"json"};

        FilePickerDialog pickerDialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);

        pickerDialog.setTitle("Select json file");
        pickerDialog.setDialogSelectionListener(selections -> selectComponentToImport(selections[0]));

        pickerDialog.show();
    }

    private void selectComponentToImport(String path) {
        var readResult = ComponentsHandler.readComponents(path);
        if (readResult.first.isPresent()) {
            SketchwareUtil.toastError(readResult.first.get());
            return;
        }
        var components = readResult.second;

        var componentNames = components.stream()
                .map(component -> (String) component.get("name"))
                .collect(Collectors.toList());
        if (componentNames.size() > 1) {
            var dialog = new aB(this);
            dialog.b(Helper.getResString(R.string.logic_editor_title_select_component));
            var choiceToImport = new AtomicInteger(-1);
            var listView = new ListView(this);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, componentNames);
            listView.setAdapter(arrayAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                choiceToImport.set(position);
            });
            dialog.a(listView);
            dialog.b(Helper.getResString(R.string.common_word_import), v -> {
                int position = choiceToImport.get();
                var component = components.get(position);
                if (position != -1 && ComponentsHandler.isValidComponent(component)) {
                    setupViews(component);
                } else {
                    SketchwareUtil.toastError(Helper.getResString(R.string.invalid_component));
                }
                dialog.dismiss();
            });
            dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
            dialog.show();
        } else {
            var component = components.get(0);
            if (ComponentsHandler.isValidComponent(component)) {
                setupViews(component);
            } else {
                SketchwareUtil.toastError(Helper.getResString(R.string.invalid_component));
            }
        }
    }
}