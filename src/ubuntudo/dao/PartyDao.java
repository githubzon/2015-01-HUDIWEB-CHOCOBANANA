package ubuntudo.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import support.Qrys;
import support.QrysP;
import support.QrysU;
import ubuntudo.model.PartyEntity;
import ubuntudo.model.TodoEntity;
import ubuntudo.model.UserEntity;

@Repository
public class PartyDao {
	private static final Logger logger = LoggerFactory.getLogger(PartyDao.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void initialize() {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		DatabasePopulatorUtils.execute(populator, jdbcTemplate.getDataSource());
	}

	public int insertPartyDao(PartyEntity party) {
		logger.debug("inserting: " + party.toString());
		return jdbcTemplate.update(QrysP.INSERT_PARTY, party.getGid(), party.getLeaderId(),
				party.getPartyName());
	}

	public long getLastPartyId() {
		RowMapper<Long> rowMapper = new RowMapper<Long>() {

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong("last_id");
			}
		};
		return jdbcTemplate.queryForObject(Qrys.GET_LAST_ID, rowMapper);
	}

	public int insertUserToPartyDao(long partyId, long userId) {
		logger.debug("inserting user to party... partyId: " + partyId + ", userId: " + userId);
		return jdbcTemplate.update(QrysU.INSERT_USER_TO_PARTY, userId, partyId);
	}

	public List<PartyEntity> retrievePartyListSearchDao(String partyName) {
		logger.debug("searching Party by Party name... partyName: " + partyName);
		RowMapper<PartyEntity> rowMapper = new RowMapper<PartyEntity>() {
			public PartyEntity mapRow(ResultSet rs, int rowNum) {
				try {
					return new PartyEntity(rs.getLong("pid"), rs.getLong("gid"),
							rs.getLong("party_leader_id"), rs.getString("p_name"),
							rs.getString("deleted"));
				} catch (SQLException e) {
					throw new BeanInstantiationException(PartyEntity.class, e.getMessage(), e);
				}
			}
		};
		return jdbcTemplate.query(QrysP.RETRIEVE_PARTY_LIST, rowMapper, Qrys.makeLikeParam(partyName));
	}

	public int updatePartyDao(PartyEntity party) {
		logger.debug("updating current party to: " + party);
		return jdbcTemplate.update(QrysP.UPDATE_PARTY, party.getLeaderId(), party.getPartyName(),
				party.getStatus(), party.getPid());
	}

	public List<Map<String, Object>> retrievePartyInGuildDao(long uid, long gid) {
		logger.debug("searching Party in guild... guild id: " + gid);
		return jdbcTemplate.queryForList(QrysP.RETRIEVE_PARTY_IN_GUILD, uid, gid);
	}

	public List<Map<String, Object>> retrievePartyListOfMyGuildsDao(long uid) {
		logger.debug("retrieving parties in my guild... uid: " + uid);
		return jdbcTemplate.queryForList(QrysP.RETRIEVE_PARTY_LIST_OF_MY_GUILDS, uid, uid);
	}

	public String getPartyName(Long pid) {
		logger.debug("get partyname for pid = {}", pid);
		RowMapper<String> rowMapper = new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("partyName");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.GET_PNAME, rowMapper, pid);
	}

	public String getGuildName(Long pid) {
		logger.debug("get guildname for pid = {}", pid);
		RowMapper<String> rowMapper = new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("guildName");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.GET_GNAME, rowMapper, pid);
	}

	public Integer getMemberNum(Long pid) {
		logger.debug("get memberNum for pid = {}", pid);
		RowMapper<Integer> rowMapper = new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt("memberCount");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.GET_PARTY_MEMBER_NUMBER, rowMapper, pid);
	}

	public Integer getTodoNum(Long pid) {
		logger.debug("get todoNum for pid = {}", pid);
		RowMapper<Integer> rowMapper = new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt("todoCount");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.GET_PARTY_TODO_NUMBER, rowMapper, pid);
	}

	public ArrayList<UserEntity> getTopUserList(Long pid) {
		logger.debug("get top user list for pid = {}", pid);
		ArrayList<UserEntity> users = new ArrayList<UserEntity>();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(QrysP.GET_TOP3_MEMBERS, pid);
		for (Map<String, Object> row : rows) {
			UserEntity user = new UserEntity((String) row.get("name"), (String) row.get("email"),
					(Long) row.get("count"));
			users.add(user);
		}
		return users;
	}

	public Float getComplteRatio(Long pid) {
		logger.debug("get complete ratio");
		RowMapper<Float> rowMapper = new RowMapper<Float>() {
			@Override
			public Float mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getFloat("completeRatio");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.GET_RATIO, rowMapper, pid, pid);
	}

	public Integer isUserSignUp(Long uid, Long pid) {
		logger.debug("is user sign up");
		RowMapper<Integer> rowMapper = new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt("count");
			}
		};
		return jdbcTemplate.queryForObject(QrysP.IS_USER_SIGNUP, rowMapper, uid, pid);	
	}

	public ArrayList<TodoEntity> getPartyTodos(Long pid) {
		logger.debug("getPartyTodos");
		ArrayList<TodoEntity> todos = new ArrayList<TodoEntity>();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(QrysP.GET_PARTY_TODO_LIST, pid);
		for (Map<String, Object> row : rows) {
			TodoEntity todo = new TodoEntity(Long.valueOf((int)row.get("tid")), Long.valueOf((int) row.get("pid")), (String)row.get("title"), (String)row.get("contents"),
					((Timestamp)row.get("duedate")).toString(), (String)row.get("status"), Long.valueOf((int)row.get("editer_id")), (String)row.get("p_name"));
			todos.add(todo);
		}
		return todos;
	}
}