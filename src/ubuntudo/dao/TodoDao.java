package ubuntudo.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import ubuntudo.JDBCManager;
import ubuntudo.model.TodoEntity;

public class TodoDao extends JDBCManager{

	public ArrayList<TodoEntity> getPersonalTodos(Long uid) {
		conn = getConnection();
		ArrayList<TodoEntity> todos = new ArrayList<TodoEntity>();
		
		try {
			String sql = "SELECT * FROM todo WHERE tid IN (SELECT tid FROM todo_user_relation WHERE uid = ? AND completed = 'trel00') ORDER BY dueDate";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, uid);
			resultSet = pstmt.executeQuery();
			System.out.println(resultSet.toString());
			if (resultSet.next()) {
				TodoEntity todo = new TodoEntity(resultSet.getLong("tid"),
						resultSet.getLong("pid"),
						resultSet.getString("title"),
						resultSet.getString("contents"),
						resultSet.getDate("duedate"),
						resultSet.getString("status"),
						resultSet.getLong("editer_id"));
				todos.add(todo);
		
			}
		} catch (SQLException e) {
			System.out.println("DB getPersonalTodos Error: " + e.getMessage());
		} finally {
			close(resultSet, pstmt, conn);
		}
		return todos;
	}
	
	public TodoEntity addPersonalTodo(TodoEntity todo) {
		TodoEntity result = null;
		Long tid = null;

		conn = getConnection();
		//아래는 트랜잭션으로 이뤄져야할 부분
		try {
			String insertTodoSql = "INSERT INTO todo VALUES (null,?,?,?,?,'todo01',?)";
			String lastTodoIdSql = "SELECT LAST_INSERT_ID() tid";
			String getLastTodoSql = "SELECT * FROM todo WHERE tid = ?";
			String insertHistorySql = "INSERT INTO content_history VALUES(null,?,?,?,?,?, now(), ?,?)";
			String insertRelationSql = "INSERT INTO todo_user_relation VALUES(?,?,'trel00')";
			
			pstmt = conn.prepareStatement(insertTodoSql);
			pstmt.setLong(1, todo.getPid());
			pstmt.setString(2, todo.getTitle());
			pstmt.setString(3, todo.getContents());
			pstmt.setDate(4, todo.getDueDate());
			pstmt.setLong(5, todo.getEditerId());
			pstmt.executeUpdate();
			
			pstmt = conn.prepareStatement(lastTodoIdSql);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
					tid = resultSet.getLong("tid");		
			}
			
			pstmt = conn.prepareStatement(getLastTodoSql);
			pstmt.setLong(1, tid);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				result = new TodoEntity(resultSet.getLong("tid"),
						resultSet.getLong("pid"),
						resultSet.getString("title"),
						resultSet.getString("contents"),
						resultSet.getDate("duedate"),
						resultSet.getString("status"),
						resultSet.getLong("editer_id"));
			}
			
			pstmt = conn.prepareStatement(insertHistorySql);
			pstmt.setLong(1, tid);
			pstmt.setLong(2, result.getPid());
			pstmt.setString(3, result.getTitle());
			pstmt.setString(4, result.getContents());
			pstmt.setDate(5, result.getDueDate());
			pstmt.setString(6, result.getStatus());
			pstmt.setLong(7, result.getEditerId());
			pstmt.executeUpdate();
			
			pstmt = conn.prepareStatement(insertRelationSql);
			pstmt.setLong(1, tid);
			pstmt.setLong(2, result.getEditerId());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("DB getPersonalTodos Error: " + e.getMessage());
		} finally {
			close(resultSet, pstmt, conn);
		}
		return result;
	}

}
