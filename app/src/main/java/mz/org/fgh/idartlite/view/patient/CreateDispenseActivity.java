package mz.org.fgh.idartlite.view.patient;

import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.BaseActivity;
import mz.org.fgh.idartlite.base.BaseModel;
import mz.org.fgh.idartlite.base.BaseViewModel;
import mz.org.fgh.idartlite.common.DialogListener;
import mz.org.fgh.idartlite.common.Listble;
import mz.org.fgh.idartlite.common.ListbleAdapter;
import mz.org.fgh.idartlite.common.ValorSimples;
import mz.org.fgh.idartlite.databinding.ActivityCreateDispenseBinding;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispenseType;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.Patient;
import mz.org.fgh.idartlite.model.PrescribedDrug;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.TherapeuticLine;
import mz.org.fgh.idartlite.model.TherapeuticRegimen;
import mz.org.fgh.idartlite.service.PrescriptionService;
import mz.org.fgh.idartlite.util.DateUtilitis;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.viewmodel.DispenseVM;
import mz.org.fgh.idartlite.viewmodel.EpisodeVM;

public class CreateDispenseActivity extends BaseActivity  implements DialogListener {

    private ActivityCreateDispenseBinding activityCreateDispenseBinding;

    private List<Listble> selectedDrugs;

    private RecyclerView rcvSelectedDrugs;

    private ListbleAdapter listbleAdapter;

    private Patient patient;

    private Prescription prescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityCreateDispenseBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_dispense);

        rcvSelectedDrugs = activityCreateDispenseBinding.rcvSelectedDrugs;

        activityCreateDispenseBinding.drugsDataLyt.setVisibility(View.GONE);
        getRelatedViewModel().setInitialDataVisible(true);
        getRelatedViewModel().setDrugDataVisible(false);

        Intent intent = this.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                getRelatedViewModel().getDispense().setPrescription((Prescription) bundle.getSerializable("prescription"));
                activityCreateDispenseBinding.setViewModel(getRelatedViewModel());

                this.prescription = getRelatedViewModel().getDispense().getPrescription();

                if (prescription == null) {

                    this.setPatient((Patient) bundle.getSerializable("patient"));

                    try {
                        this.prescription = getRelatedViewModel().getLastPatientPrescription(this.getPatient());

                        getRelatedViewModel().getDispense().setPrescription((this.prescription ));

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        populateForm();

        activityCreateDispenseBinding.dispenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        activityCreateDispenseBinding.dispenseDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        activityCreateDispenseBinding.nextPickupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDispenseActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        activityCreateDispenseBinding.nextPickupDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        activityCreateDispenseBinding.initialData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeVisibilityToInitialData(view);

            }
        });
        activityCreateDispenseBinding.txvDrugs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeVisibilityToInitialData(view);

            }
        });

        activityCreateDispenseBinding.imvAddSelectedDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDrugs == null) selectedDrugs = new ArrayList<>();

                if (activityCreateDispenseBinding.spnDrugs.getSelectedItem() != null) {
                    Listble listble = (Listble) activityCreateDispenseBinding.spnDrugs.getSelectedItem();

                    if (!selectedDrugs.contains(listble)) {
                        listble.setListPosition(selectedDrugs.size() + 1);
                        selectedDrugs.add(listble);
                        Collections.sort(selectedDrugs);

                        displaySelectedDrugs();
                    } else {

                        Utilities.displayAlertDialog(CreateDispenseActivity.this, getString(R.string.drug_data_duplication_msg)).show();
                    }

                }
            }
        });

    }

    private void populateForm() {

        try {

            List<Drug> drugs = new ArrayList<>();
            drugs.add(new Drug());
            drugs.addAll(getRelatedViewModel().getAllDrugs());

            ArrayAdapter<Drug> drugArrayAdapter = new ArrayAdapter<Drug>(getApplicationContext(), android.R.layout.simple_spinner_item, drugs);
            drugArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            activityCreateDispenseBinding.spnDrugs.setAdapter(drugArrayAdapter);

            List<ValorSimples> durations = new ArrayList<>();
            durations.add(new ValorSimples());
            durations.add(ValorSimples.fastCreate(2, Prescription.DURATION_TWO_WEEKS));
            durations.add(ValorSimples.fastCreate(4, Prescription.DURATION_ONE_MONTH));
            durations.add(ValorSimples.fastCreate(8, Prescription.DURATION_TWO_MONTHS));
            durations.add(ValorSimples.fastCreate(12, Prescription.DURATION_THREE_MONTHS));
            durations.add(ValorSimples.fastCreate(16, Prescription.DURATION_FOUR_MONTHS));
            durations.add(ValorSimples.fastCreate(20, Prescription.DURATION_FIVE_MONTHS));
            durations.add(ValorSimples.fastCreate(24, Prescription.DURATION_SIX_MONTHS));

            ArrayAdapter<ValorSimples> valorSimplesArrayAdapter = new ArrayAdapter<ValorSimples>(getApplicationContext(), android.R.layout.simple_spinner_item, durations);
            valorSimplesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            activityCreateDispenseBinding.spnDuration.setAdapter(valorSimplesArrayAdapter);

        } catch (SQLException e) {
            Utilities.displayAlertDialog(CreateDispenseActivity.this, getString(R.string.error_loading_form_data)+e.getMessage());
            e.printStackTrace();
        }
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    private void changeVisibilityToInitialData(View view) {
        if (view.equals(activityCreateDispenseBinding.initialData)) {
            if (activityCreateDispenseBinding.initialDataLyt.getVisibility() == View.VISIBLE) {
                activityCreateDispenseBinding.initialDataLyt.setVisibility(View.GONE);
                switchLayout();
            } else {
                activityCreateDispenseBinding.initialDataLyt.setVisibility(View.VISIBLE);
                switchLayout();
            }
        } else if (view.equals(activityCreateDispenseBinding.txvDrugs)) {
            if (activityCreateDispenseBinding.drugsDataLyt.getVisibility() == View.VISIBLE) {
                activityCreateDispenseBinding.drugsDataLyt.setVisibility(View.GONE);
                switchLayout();
            } else {
                activityCreateDispenseBinding.drugsDataLyt.setVisibility(View.VISIBLE);
                switchLayout();
            }
        }
    }

    private void switchLayout() {
        getRelatedViewModel().setInitialDataVisible(!getRelatedViewModel().isInitialDataVisible());
        getRelatedViewModel().setDrugDataVisible(!getRelatedViewModel().isDrugDataVisible());
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(DispenseVM.class);
    }

    @Override
    public DispenseVM getRelatedViewModel() {
        return (DispenseVM) super.getRelatedViewModel();
    }

    private void displaySelectedDrugs() {
        if (listbleAdapter != null) {
            listbleAdapter.notifyDataSetChanged();
        } else {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            rcvSelectedDrugs.setLayoutManager(mLayoutManager);
            rcvSelectedDrugs.setItemAnimator(new DefaultItemAnimator());
            rcvSelectedDrugs.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));

            listbleAdapter = new ListbleAdapter(rcvSelectedDrugs, this.selectedDrugs, this);
            rcvSelectedDrugs.setAdapter(listbleAdapter);
        }

    }

    public void loadFormData() throws SQLException {
        getRelatedViewModel().getDispense().setSupply(((ValorSimples) activityCreateDispenseBinding.spnDuration.getSelectedItem()).getId());
        getRelatedViewModel().getDispense().setPickupDate(DateUtilitis.createDate(activityCreateDispenseBinding.dispenseDate.getText().toString(), "dd-MM-YYYY"));
        getRelatedViewModel().getDispense().setUuid(Utilities.getNewUUID().toString());
        getRelatedViewModel().getDispense().setSyncStatus(BaseModel.SYNC_SATUS_READY);
        getRelatedViewModel().getDispense().setNextPickupDate(DateUtilitis.createDate(activityCreateDispenseBinding.dispenseDate.getText().toString(), "dd-MM-YYYY"));

        List<DispensedDrug> dispensedDrugs = new ArrayList<>();

        for (Listble drug : this.selectedDrugs){

            dispensedDrugs.add(initNewPrescribedDrug((Drug) drug));
        }

        getRelatedViewModel().getDispense().setDispensedDrugs(dispensedDrugs);

    }

    private DispensedDrug initNewPrescribedDrug(Drug drug) throws SQLException {

        int quantitySupplied = Integer.valueOf(getRelatedViewModel().getDispense().getSupply()) * drug.getPackSize();

        List<Stock> stocks = getRelatedViewModel().getAllStocksByClinicAndDrug(getCurrentClinic(), drug);

        DispensedDrug dispensedDrug = new DispensedDrug();

        //Ainda por melhorar pra pegar stock ainda com validade e gerar novo objecto DispenseDrug caso o primeiro nao cubra
        //a quantidade aviada
        for (Stock stock:
             stocks) {

            int stockQuantity = stock.getQuantity();

            if(quantitySupplied < stockQuantity){

                dispensedDrug.setQuantitySupplied(quantitySupplied);
                dispensedDrug.setStock(stock);
                dispensedDrug.setSyncStatus(BaseModel.SYNC_SATUS_READY);
                break;
            }

        }
        return dispensedDrug;
    }

    @Override
    public void doOnConfirmed() {

    }
}