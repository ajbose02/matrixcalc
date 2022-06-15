package com.example.matrixcalc;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;

import io.github.kexanie.library.MathView;


public class MainActivity extends AppCompatActivity {

    RelativeLayout parentLayout;
    Button plusRow;
    Button plusCol;
    Button minusRow;
    Button minusCol;
    TableLayout matrix;
    Button reduceButton;
    Button rankButton;
    Button clearButton;
    Button detButton;
    Button invButton;
    Button addButton;
    Button subtractButton;
    Button multiplyButton;
    Button equals;
    TableLayout matrix2;
    int rows = 2;
    int columns = 2;
    double[][] nums;
    double[][] nums2;
    double[][] sumMatrix;
    String operations = "";
    private static DecimalFormat df = new DecimalFormat("0.00");
    String result = "";
    MathView mathView;
    boolean dontShowSteps = false;
    double sign = 1;
    HorizontalScrollView hScroll;
    FirebaseDatabase database;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Typeface typeface = getResources().getFont(R.font.old_standard_tt);

        parentLayout = findViewById(R.id.parentLayout);
        plusRow = findViewById(R.id.plusRow);
        plusCol = findViewById(R.id.plusCol);
        matrix = findViewById(R.id.matrix);
        reduceButton = findViewById(R.id.reduceButton);
        rankButton = findViewById(R.id.rankButton);
        clearButton = findViewById(R.id.clearButton);
        mathView = findViewById(R.id.mathView);
        minusRow = findViewById(R.id.minusRow);
        minusCol = findViewById(R.id.minusCol);
        detButton = findViewById(R.id.detButton);
        invButton = findViewById(R.id.invButton);
        addButton = findViewById(R.id.addButton);
        subtractButton = findViewById(R.id.subtractButton);
        multiplyButton = findViewById(R.id.multiplyButton);
        equals = findViewById(R.id.equals);
        TableLayout matrix2 = new TableLayout(MainActivity.this);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        myRef.setValue("Hello, World!");


        plusRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rows < 6) {
                    rows++;
                    TableRow row = new TableRow(MainActivity.this);
                    for (int i = 0; i < columns; i++) {
                        EditText cell = new EditText(MainActivity.this);
                        cell.setWidth(180);
                        cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        cell.setBackgroundResource(android.R.drawable.edit_text);
                        cell.setTypeface(typeface);
                        row.addView(cell);
                    }
                    matrix.addView(row);
                } else
                    Toast.makeText(MainActivity.this, "Cannot make more rows!", Toast.LENGTH_SHORT).show();
            }
        });

        plusCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (columns < 6) {
                    columns++;
                    for (int i = 0; i < rows; i++) {
                        TableRow row = (TableRow) matrix.getChildAt(i);
                        EditText cell = new EditText(MainActivity.this);
                        cell.setWidth(180);
                        cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        cell.setBackgroundResource(android.R.drawable.edit_text);
                        cell.setTypeface(typeface);
                        row.addView(cell);
                    }
                } else
                    Toast.makeText(MainActivity.this, "Cannot make more columns!", Toast.LENGTH_SHORT).show();
            }
        });

        minusRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rows>2) {
                    matrix.removeViewAt(rows - 1);
                    rows--;
                }
                else
                    Toast.makeText(MainActivity.this, "Cannot remove more rows!", Toast.LENGTH_SHORT).show();
            }
        });

        minusCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(columns>2){
                    for(int i = 0; i < rows; i++){
                        TableRow row = (TableRow) matrix.getChildAt(i);
                        row.removeViewAt(columns-1);
                    }
                    columns--;
                }
                else
                    Toast.makeText(MainActivity.this, "Cannot remove more columns!", Toast.LENGTH_SHORT).show();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableButtons();
                if(matrix2.getParent()!=null) { //matrix2 exists, meaning matrix1 DNE
                    parentLayout.removeView(matrix2);
                    parentLayout.addView(matrix);
                }
                rows = 2;
                columns = 2;
                matrix.removeAllViews();
                mathView.setText("");
                result = "";

                for (int i = 0; i < 2; i++) {
                    TableRow row = new TableRow(MainActivity.this);
                    for (int j = 0; j < 2; j++) {
                        EditText cell = new EditText(MainActivity.this);
                        cell.setWidth(180);
                        cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        cell.setBackgroundResource(android.R.drawable.edit_text);
                        cell.setTypeface(typeface);
                        row.addView(cell);
                    }
                    matrix.addView(row);
                }
            }
        });

        reduceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dontShowSteps = false;
                    operations = "";
                    result = "\\(Reduction: \\)";
                    makeMatrix();
                    convert();
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    operations = "";
                    dontShowSteps = true;
                    result = "\\(Reduction: \\)";
                    makeMatrix();
                    convert();
                    displayResult(nums, "M");
                    int rank = rows - countZeroRows();
                    result += "\\(Rank: " + String.valueOf(rank) + "\\)";
                    mathView.setText(result);
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        detButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                operations = "";
                sign = 1;
                dontShowSteps = true;
                result = "\\(Reduction: \\)";
                if(rows==columns) {
                    makeMatrix();
                    double det = det();
                    if(det==0)
                        det = 0;
                    String str = "$$ det(M) = " + df.format(det) + " $$ ";
                    result += operations;
                    result += str;
                    mathView.setText(result);
                }
                else
                    Toast.makeText(MainActivity.this, "Need to input square matrix!", Toast.LENGTH_SHORT).show();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                operations = "";
                mathView.setText("");
                makeMatrix();
                for (int i = 0; i < rows; i++) {
                    TableRow row = new TableRow(MainActivity.this);
                    for (int j = 0; j < columns; j++) {
                        EditText cell = new EditText(MainActivity.this);
                        cell.setWidth(180);
                        cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        cell.setBackgroundResource(android.R.drawable.edit_text);
                        cell.setTypeface(typeface);
                        row.addView(cell);
                    }
                    matrix2.addView(row);
                }
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(170, 170, 0, 0);
                matrix2.setLayoutParams(params);

                if(matrix.getParent()!=null) {
                    parentLayout.removeView(matrix);
                    parentLayout.addView(matrix2);
                }
                disableButtons();
            }
        });

        equals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeNums2();
                addMatrices();
                displayResult(sumMatrix, "M+N");
            }
        });






    }

    public void disableButtons(){
        plusRow.setEnabled(false);
        plusCol.setEnabled(false);
        minusRow.setEnabled(false);
        minusCol.setEnabled(false);
    }

    public void enableButtons(){
        plusRow.setEnabled(true);
        plusCol.setEnabled(true);
        minusRow.setEnabled(true);
        minusCol.setEnabled(true);
    }

    public void displayResult(double[][] n, String name){
        if(!dontShowSteps)
            result += operations;
        result += "$$ " + name + " = \\begin{bmatrix} ";
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                if(n[row][col]%1==0)
                    result += String.valueOf((int)n[row][col]) + "&amp;";
                else
                    result += String.valueOf(df.format(n[row][col])) + "&amp;";
            }
            if(row<rows-1)
                result += "\\\\";
        }
        result += "\\end{bmatrix} $$";

        result = result.replaceAll("&amp;\\\\", "\\\\");
        mathView.setText(result);
    }




//for rank, use boolean to only display final REF result, then display the rank!

    public void makeMatrix(){
        nums = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double num = 0;
                TableRow row = (TableRow) matrix.getChildAt(i);
                EditText et = (EditText) row.getChildAt(j);
                String text = et.getText().toString();
                if (!text.equals(""))
                    num = Double.parseDouble(text);
                else{
                    et.setText("0");
                    num = 0;
                }
                nums[i][j] = num;
            }
        }
        if(!dontShowSteps)
            displayResult(nums, "M");

    }

    public void makeNums2(){
        nums2 = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double num = 0;
                if(matrix2!=null) {
                    TableRow row = (TableRow) matrix2.getChildAt(i);
                    EditText et = (EditText) row.getChildAt(j);
                    String text = et.getText().toString();
                    if (!text.equals(""))
                        num = Double.parseDouble(text);
                    else {
                        et.setText("0");
                        num = 0;
                    }
                    nums2[i][j] = num;
                }
            }
        }
        displayResult(nums2, "N");
    }

    public void addMatrices(){
        sumMatrix = new double[rows][columns];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                sumMatrix[i][j] = nums[i][j] + nums2[i][j];
            }
        }

    }

    public void convert(){
        int startRow = 0;
        int startCol = 0;
        moveZeros();
        for(int col = 0; col < nums[0].length; col++){
            for(int row = nums.length-1; row > startRow; row--){
                int end = startRow + 1;
                while(nums[startRow][startCol]==0 && end < nums.length){
                    if(nums[end][startCol]!=0) {
                        swapRows(startRow, end);
                        sign *= -1;
                        String str = "R_{" + String.valueOf(startRow + 1) + "}<->" + "R_{" + String.valueOf(end + 1) + "}";
                        operations += " $$ " + str + " $$ ";
                        break;
                    }
                    end++;
                }
                double factor = (double)nums[row][col]/(double)nums[startRow][startCol];
                if(factor > 0) {
                    String str = "R_{" + String.valueOf(row+1) + "} - " + String.valueOf(df.format(factor)) + "R_{" + String.valueOf(startRow+1) + "}";
                    operations += " $$ " + str + " $$ ";
                }
                else if(factor < 0) {
                    String str = "R_{" + String.valueOf(row+1) + "} + " + String.valueOf(df.format(Math.abs(factor))) + "R_{" + String.valueOf(startRow+1) + "}";
                    operations += " $$ " + str + " $$ ";
                }
                for(int i = 0; i < nums[row].length; i++){
                    //subtracting the rows
                    if(factor>0 || factor <=0)
                        nums[row][i] -= (double)(factor * nums[startRow][i]);
                }
            }
            if(operations!="") {
                if(!dontShowSteps)
                    displayResult(nums, "M");
                operations = "";
            } //this is the issue! algorithm is just fine.
            else
                break;

            startRow++;
            startCol++;
        }
    }

    public boolean hasRowOfZeros(double[] n){
        int count = 0;
        for(int i = 0; i < n.length; i++){
            if(n[i]==0)
                count++;
        }
        if(count==n.length)
            return true;
        return false;
    }

    public void swapRows(int ind1, int ind2){
        double[] temp = nums[ind1];
        double[] last = nums[ind2];
        nums[ind1] = last;
        nums[ind2] = temp;
    }

    public void moveZeros(){
        boolean hasRowOfZeros = false;
        for(int row = 0; row < nums.length; row++){
            if(row<nums.length-1){
                hasRowOfZeros = hasRowOfZeros(nums[row]);
                if(hasRowOfZeros)
                    swapRows(row, nums.length-1);
            }
        }
    }

    public int countZeroRows(){
        int count = 0;
        int countOne = 0;
        for(int i = 0; i < nums.length; i++){
            for(int j = 0; j < nums[i].length; j++){
                if(nums[i][j]==0)
                    countOne++;
            }
            if(countOne==columns)
                count++;
             countOne = 0;
        }
        return count;
    }

    public double det(){
        operations = "";
        convert();
        displayResult(nums, "M");
        double d = 0;
        int start = 0;
        operations = "$$ \\mathcal{D} = ";
        while(start < nums.length){
            if(start==0) {
                d = nums[start][start];
                operations += df.format(d) + " \\times ";
            }
            else {
                d *= nums[start][start];
                String time = "";
                if(start<nums.length-1)
                    time = " \\times ";
                operations += df.format(nums[start][start]) + time;
            }
            start++;
        }
        d*=sign;
        if(d==0)
            d = 0;
        operations += " = " + df.format(d) + " $$ ";
        return d;
    }




}