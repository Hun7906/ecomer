package com.hung.ecomer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private LinearLayout root, list;
    private EditText name, category, price;
    private TextView status;
    private Button save, export;
    private String editingId = null, pendingJson;
    private boolean busy = false;
    private final ArrayList<DocumentSnapshot> products = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() == null) showLogin(); else showProducts();
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density); }
    private void page(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(Color.rgb(244,248,252)); scroll.addView(root); setContentView(scroll);
        TextView heading = new TextView(this); heading.setText(title); heading.setTextSize(26);
        heading.setTextColor(Color.rgb(24,67,109)); root.addView(heading);
    }
    private TextView text(String value) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(16);
        v.setPadding(0,dp(8),0,dp(8)); root.addView(v); return v;
    }
    private EditText input(String hint, int type) {
        EditText e = new EditText(this); e.setHint(hint); e.setInputType(type);
        e.setSingleLine(true); root.addView(e, new LinearLayout.LayoutParams(-1,dp(56))); return e;
    }
    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); root.addView(b); return b;
    }
    private void stopListener() { if(listener != null) { listener.remove(); listener = null; } }
    private void message(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }

    private void showLogin() {
        stopListener(); products.clear(); busy = false;
        page("ECOMER Hung • Đăng nhập");
        text("Quản lý sản phẩm dành cho tài khoản quản trị.");
        EditText email = input("Email quản trị",InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = input("Mật khẩu",InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Button login = button("Đăng nhập");
        login.setOnClickListener(v -> {
            String e = email.getText().toString().trim(), pw = password.getText().toString();
            if(e.isEmpty() || pw.isEmpty()) { message("Nhập email và mật khẩu."); return; }
            login.setEnabled(false);
            auth.signInWithEmailAndPassword(e,pw).addOnCompleteListener(task -> {
                login.setEnabled(true);
                if(task.isSuccessful()) showProducts();
                else message("Đăng nhập thất bại. Kiểm tra tài khoản, mật khẩu và Internet.");
            });
        });
    }
    private void showProducts() {
        stopListener(); editingId = null; busy = false; products.clear();
        page("ECOMER Hung • Sản phẩm");
        text("Tài khoản: " + auth.getCurrentUser().getEmail());
        button("Đăng xuất").setOnClickListener(v -> {
            if(busy) { message("Chờ thao tác lưu hoàn tất."); return; }
            stopListener(); auth.signOut(); showLogin();
        });
        name = input("Tên sản phẩm (tối đa 100 ký tự)", InputType.TYPE_CLASS_TEXT);
        category = input("Loại sản phẩm (tối đa 100 ký tự)", InputType.TYPE_CLASS_TEXT);
        price = input("Giá VND, ví dụ 150000",InputType.TYPE_CLASS_NUMBER);
        save = button("Thêm sản phẩm"); save.setOnClickListener(v -> saveProduct());
        button("Làm mới / Hủy sửa").setOnClickListener(v -> { if(!busy) clearForm(); });
        status = text("Đang tải dữ liệu từ Firestore...");
        export = button("Xuất CSDL JSON"); export.setEnabled(false);
        export.setOnClickListener(v -> exportJson());
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); root.addView(list);
        listener = db.collection("products").addSnapshotListener(MetadataChanges.INCLUDE, (snapshot,error) -> {
            if(error != null) {
                products.clear(); list.removeAllViews(); export.setEnabled(false);
                status.setText("Không đọc được CSDL: " + error.getMessage() + "\nKiểm tra UID trong Rules, bấm Publish rồi đăng xuất/đăng nhập lại."); return;
            }
            if(snapshot == null) return;
            products.clear(); products.addAll(snapshot.getDocuments());
            products.sort((a,b) -> safe(a,"tenSanPham").compareToIgnoreCase(safe(b,"tenSanPham")));
            export.setEnabled(!snapshot.getMetadata().isFromCache() && !snapshot.getMetadata().hasPendingWrites());
            status.setText("Danh sách: " + products.size() + " sản phẩm" +
                (snapshot.getMetadata().isFromCache() ? " • bộ nhớ tạm, kiểm tra Internet" : " • Firestore"));
            renderList();
        });
    }
    private String safe(DocumentSnapshot d,String field) {
        Object value = d.get(field); return value == null ? "" : value.toString();
    }
    private void renderList() {
        list.removeAllViews();
        for(DocumentSnapshot d : products) {
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12),dp(10),dp(12),dp(10)); card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(10); list.addView(card,lp);
            TextView info = new TextView(this); info.setTextSize(17);
            Object value = d.get("gia");
            String formatted = value instanceof Number ? NumberFormat.getNumberInstance(new Locale("vi","VN")).format(value) : "?";
            info.setText("Tên: " + safe(d,"tenSanPham") + "\nLoại: " + safe(d,"loaiSanPham") + "\nGiá: " + formatted + " đ"); card.addView(info);
            LinearLayout actions = new LinearLayout(this); card.addView(actions);
            Button edit = new Button(this); edit.setText("Sửa"); actions.addView(edit);
            Button delete = new Button(this); delete.setText("Xóa"); actions.addView(delete);
            edit.setOnClickListener(v -> {
                if(busy) return;
                editingId=d.getId(); name.setText(safe(d,"tenSanPham")); category.setText(safe(d,"loaiSanPham"));
                price.setText(value instanceof Number ? String.valueOf(((Number)value).longValue()) : "");
                save.setText("Lưu thay đổi"); name.requestFocus();
                ((ScrollView)root.getParent()).smoothScrollTo(0,0);
            });
            delete.setOnClickListener(v -> {
                if(busy) return;
                new AlertDialog.Builder(this).setTitle("Xóa sản phẩm?")
                    .setMessage(safe(d,"tenSanPham")).setNegativeButton("Hủy",null)
                    .setPositiveButton("Xóa",(dialog,which) -> {
                        if(busy) return;
                        setBusy(true);
                        db.collection("products").document(d.getId()).delete()
                          .addOnSuccessListener(x -> { if(d.getId().equals(editingId)) clearForm(); message("Đã xóa trên Firestore."); })
                          .addOnFailureListener(e -> message("Xóa thất bại: " + e.getMessage()))
                          .addOnCompleteListener(t -> setBusy(false));
                    }).show();
            });
        }
    }
    private void setBusy(boolean value) { busy=value; save.setEnabled(!value); }
    private void clearForm() { editingId=null; name.setText(""); category.setText(""); price.setText(""); save.setText("Thêm sản phẩm"); }
    private void saveProduct() {
        if(busy) return;
        String n=name.getText().toString().trim(), c=category.getText().toString().trim(), raw=price.getText().toString().trim();
        if(n.isEmpty() || c.isEmpty() || n.length()>100 || c.length()>100) { message("Tên và loại phải có từ 1 đến 100 ký tự."); return; }
        long amount;
        try { amount=Long.parseLong(raw); if(amount<0 || amount>1000000000L) throw new NumberFormatException(); }
        catch(NumberFormatException e) { message("Giá là số nguyên từ 0 đến 1.000.000.000. Nhập không có dấu chấm/phẩy."); return; }
        Map<String,Object> data=new HashMap<>(); data.put("tenSanPham",n); data.put("loaiSanPham",c); data.put("gia",amount);
        DocumentReference ref = editingId==null ? db.collection("products").document() : db.collection("products").document(editingId);
        setBusy(true);
        com.google.android.gms.tasks.Task<Void> task = editingId==null ? ref.set(data) : ref.update(data);
        task.addOnSuccessListener(x -> { clearForm(); message("Đã lưu trên Firestore."); })
            .addOnFailureListener(e -> message("Lưu thất bại: " + e.getMessage()))
            .addOnCompleteListener(t -> setBusy(false));
    }
    private void exportJson() {
        if(busy) { message("Chờ lưu xong rồi xuất JSON."); return; }
        export.setEnabled(false);
        db.collection("products").get(Source.SERVER).addOnSuccessListener(snap -> {
            try {
                JSONObject records = new JSONObject();
                for(DocumentSnapshot d : snap.getDocuments()) records.put(d.getId(),new JSONObject(d.getData()));
                JSONObject json = new JSONObject(); json.put("products",records); pendingJson=json.toString(2);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json"); intent.putExtra(Intent.EXTRA_TITLE,"ecomer-csdl.json"); startActivityForResult(intent,100);
            } catch(Exception e) { message("Không xuất được JSON: " + e.getMessage()); }
        }).addOnFailureListener(e -> message("Cần Internet và quyền đọc để xuất JSON: " + e.getMessage()))
          .addOnCompleteListener(t -> export.setEnabled(true));
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==100 && result==RESULT_OK && data!=null && data.getData()!=null && pendingJson!=null) {
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())) {
                if(out==null) throw new java.io.IOException("Không mở được tệp");
                out.write(pendingJson.getBytes(StandardCharsets.UTF_8)); message("Đã lưu tệp JSON.");
            } catch(Exception e) { message("Lỗi ghi tệp: " + e.getMessage()); }
        }
        pendingJson=null;
    }
    @Override protected void onDestroy() { stopListener(); super.onDestroy(); }
}
