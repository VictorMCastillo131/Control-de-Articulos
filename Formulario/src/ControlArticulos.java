import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ControlArticulos {
    private JFrame frame;
    private JTextField txtCodigo, txtDescripcion, txtPrecio;
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection conn;

    public ControlArticulos() {
        initializeDB();
        initializeUI();
    }

    // Configurar conexión a MySQL
    private void initializeDB() {
        try {
            // Registrar el driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // se conecta con la base de datos
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/base1?serverTimezone=America/Mexico_City", "root", "");
            System.out.println("Conexión exitosa a la base de datos MySQL.");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error: Driver JDBC no encontrado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void initializeUI() {
        frame = new JFrame("Control de Artículos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new FlowLayout());

        // Componentes
        JLabel lblCodigo = new JLabel("Código:");
        txtCodigo = new JTextField(5);
        JButton btnBuscar = new JButton("Buscar");

        JLabel lblDescripcion = new JLabel("Descripción:");
        txtDescripcion = new JTextField(15);

        JLabel lblPrecio = new JLabel("Precio:");
        txtPrecio = new JTextField(7);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");

        tableModel = new DefaultTableModel(new String[]{"Código", "Descripción", "Precio"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Añadir componentes
        frame.add(lblCodigo);
        frame.add(txtCodigo);
        frame.add(btnBuscar);

        frame.add(lblDescripcion);
        frame.add(txtDescripcion);

        frame.add(lblPrecio);
        frame.add(txtPrecio);

        frame.add(btnAgregar);
        frame.add(btnModificar);
        frame.add(btnEliminar);

        frame.add(scrollPane);

        // Asignar acciones
        btnBuscar.addActionListener(e -> buscarArticulo());
        btnAgregar.addActionListener(e -> agregarArticulo());
        btnModificar.addActionListener(e -> modificarArticulo());
        btnEliminar.addActionListener(e -> eliminarArticulo());

        cargarTabla();
        frame.setVisible(true);
    }

    @SuppressWarnings({"ConvertToTryWithResources", "CallToPrintStackTrace"})
    private void buscarArticulo() {
        if (txtCodigo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El campo Código no puede estar vacío.");
            return;
        }
        try {
            String codigo = txtCodigo.getText();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM articulos WHERE codigo = ?");
            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtDescripcion.setText(rs.getString("descripcion"));
                txtPrecio.setText(String.valueOf(rs.getFloat("precio")));
                JOptionPane.showMessageDialog(frame, "Artículo encontrado.");
            } else {
                JOptionPane.showMessageDialog(frame, "Artículo no existe.");
                limpiarCampos();
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"CallToPrintStackTrace", "ConvertToTryWithResources"})
    private void agregarArticulo() {
        if (!validarCampos()) return;

        try {
            String codigo = txtCodigo.getText();
            String descripcion = txtDescripcion.getText();
            float precio = Float.parseFloat(txtPrecio.getText());

            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM articulos WHERE codigo = ?");
            checkStmt.setString(1, codigo);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(frame, "Ya existe el código.");
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO articulos (codigo, descripcion, precio) VALUES (?, ?, ?)");
                insertStmt.setString(1, codigo);
                insertStmt.setString(2, descripcion);
                insertStmt.setFloat(3, precio);
                insertStmt.executeUpdate();

                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(frame, "Artículo agregado.");
                insertStmt.close();
            }
            rs.close();
            checkStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"ConvertToTryWithResources", "CallToPrintStackTrace"})
    private void modificarArticulo() {
        if (!validarCampos()) return;

        try {
            String codigo = txtCodigo.getText();
            String descripcion = txtDescripcion.getText();
            float precio = Float.parseFloat(txtPrecio.getText());

            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE articulos SET descripcion = ?, precio = ? WHERE codigo = ?");
            pstmt.setString(1, descripcion);
            pstmt.setFloat(2, precio);
            pstmt.setString(3, codigo);

            if (pstmt.executeUpdate() > 0) {
                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(frame, "Artículo modificado.");
            } else {
                JOptionPane.showMessageDialog(frame, "Código no encontrado.");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"ConvertToTryWithResources", "CallToPrintStackTrace"})
    private void eliminarArticulo() {
        if (txtCodigo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El campo Código no puede estar vacío.");
            return;
        }
        try {
            String codigo = txtCodigo.getText();
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM articulos WHERE codigo = ?");
            pstmt.setString(1, codigo);

            if (pstmt.executeUpdate() > 0) {
                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(frame, "Artículo eliminado.");
            } else {
                JOptionPane.showMessageDialog(frame, "Código no encontrado.");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"CallToPrintStackTrace", "ConvertToTryWithResources"})
    private void cargarTabla() {
        try {
            tableModel.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM articulos");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getFloat("precio")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    private boolean validarCampos() {
        if (txtCodigo.getText().isEmpty() || txtDescripcion.getText().isEmpty() || txtPrecio.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Todos los campos deben estar llenos.");
            return false;
        }
        try {
            Float.parseFloat(txtPrecio.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "El campo Precio debe ser numérico.");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControlArticulos::new);
    }
}
