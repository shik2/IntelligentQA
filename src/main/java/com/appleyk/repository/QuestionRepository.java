package com.appleyk.repository;

import com.appleyk.node.Movie;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * 基于电影知识图谱的自问自答的查询接口
 * @author yukun24@126.com
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年5月10日-下午3:48:51
 */
public interface QuestionRepository extends Neo4jRepository<Movie,Long> {

	/**
	 * 0 对应问题模板0 == 小区地址
	 *
	 */
	@Query("match(n:community)-[r:地址]->(b:address) where n.community={community} return b.title")
	String getAddress(@Param("community") String community);

	/**
	 * 1 对应问题模板1 == 小区附近医院
	 *
	 */
	@Query("match(n:community)-[r:附近医院]->(b:hospital) where n.community={community} return b.title")
	List<String> getHospital(@Param("community") String community);

	/**
	 * 2 对应问题模板2 == 小区附近学校
	 *
	 */
	@Query("match(n:community)-[r:附近大学]->(b:uschool) where n.community={community} return b.title")
	List<String> getUschool(@Param("community") String community);

	@Query("match(n:community)-[r:附近中小学]->(b:pschool) where n.community={community} return b.title")
	List<String> getPschool(@Param("community") String community);

	@Query("match(n:community)-[r:附近幼儿园]->(b:nschool) where n.community={community} return b.title")
	List<String> getNschool(@Param("community") String community);

	/**
	 * 3 对应问题模板3 == 小区交通
	 *
	 */
	@Query("match(n:community)-[r:交通路线]->(b:transport) where n.community={community} return b.title")
	List<String> getTransport(@Param("community") String title);

	/**
	 * 5 对应问题模板5 == 小区商场
	 *
	 */
	@Query("match(n:community)-[r:购物点]->(b:market) where n.community={community} return b.title")
	List<String> getMarkets(@Param("community") String title);

	/**
	 * 6 对应问题模板6 == 小区银行
	 *
	 */
	@Query("match(n:community)-[r:附近银行]->(b:bank) where n.community={community} return b.title")
	List<String> getBanks(@Param("community") String title);

	/**
	 * 7 对应问题模板7 == 建造年份
	 *
	 */
	@Query("match(n:community) where n.community={community} return n.built_day")
	String getBuiltDay(@Param("community") String title);

	/**
	 * 6 对应问题模板6 == nnt(演员) ng(电影类型) 电影作品
	 *
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} "
			+ "match(g:Genre)-[:is]-(m) where g.name=~{gname} return distinct  m.title")
	List<String> getActorMoviesByType(@Param("name") String name, @Param("gname") String gname);

	/**
	 * 7对应问题模板7 == nnt(演员) 电影作品
	 * 
	 * @param name
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]->(m:Movie) where n.name={name} return m.title")
	List<String> getActorMovies(@Param("name") String name);

	/**
	 * 8对应问题模板8 == nnt 参演评分 大于 x(电影评分)
	 * 
	 * @param name 演员姓名
	 * @param score 电影分数
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} and m.rating > {score} return m.title")
	List<String> getActorMoviesByHScore(@Param("name") String name,@Param("score") Double score);
	
	
	/**
	 * 9对应问题模板9 == nnt 参演评分 小于 x(电影评分)
	 * 
	 * @param name 演员姓名
	 * @param score 电影分数
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} and m.rating < {score} return m.title")
	List<String> getActorMoviesByLScore(@Param("name") String name,@Param("score") Double score);
	
	
	/**
	 * 10 对应问题模板10 == nnt(演员) 电影类型
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员出演过的所有电影的类型集合【不重复的】
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} "
			+ "match(p:Genre)-[:is]-(m) return distinct  p.name")
	List<String> getActorMoviesType(@Param("name") String name);

	
	/**
	 * 12 对应问题模板12 == nnt(演员) 电影数量
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员出演过的所有电影的类型集合【不重复的】
	 */
	@Query("match(n)-[:actedin]-(m) where n.name ={name} return count(*)")
	Integer getMoviesCount(@Param("name") String name);
	
	/**
	 * 13 对应问题模板13 == nnt(演员) 出生日期
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员的出生日期
	 */
	@Query("match(n:Person) where n.name={name} return n.birth")
	String getActorBirth(@Param("name") String name);
	
}
