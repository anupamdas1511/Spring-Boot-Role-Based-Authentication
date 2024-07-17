# Notes

## How to use Spring Security

- Create a User data class

```java
import java.util.List;

@Document("users")
@Data
public class User {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    @NonNull
    private String username;
    private String email;
    @NonNull
    private String password;

    private List<String> roles;
    @CreatedDate
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- Create an implementation of UserDetailService interface from Spring Security

```java
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; // create this to connect to db

    @Autowired
    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().toArray(new String[0]))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
```

- Create a Configuration class for Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/article/**", "/user/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                ).httpBasic(Customizer.withDefaults()); // Uses Basic Auth Authentication

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Implementation of JWT Authentication

### What is JWT?

Json Web Token or JWT is a compact URL-safe means of representing claims to be transferred between two parties.

JWT is a **string** consists of 3 parts separated by dots. These 3 parts are header, payload and signature.

<h4>Header</h4>

**Header** typically consists of 2 parts: the type of the token(JWT) and the signing algorithm being used, such as HMAC SHA256 or RSA.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

<h4>Payload</h4>

The **payload** contains the Claims. Claims are statements about the entity(typically the user) and additional metadata.

```json
{
  "email": "johndoe@example.com",
  "name": "John Doe"
}
```

<h4>Signature</h4>

The **signature** is used to verify that the sender of the JWT is who it says it is and to ensure the message wasn't change along the way.

To create the signature part, you have to take the encoded header, the encoded payload, the secret, the algorithm specified in the header, and sign that.

```js
HMACSHA256(
    secret,
    base64UrlEncode(header) + "." + base64UrlEncode(payload)
)
```

### How to implement JWT in the project

- Create a utility class for Jwt e.g. JwtUtil in this case

This class will contain methods for creating tokens, setting claims, handle expiry of token and other utilities.

```java
@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String SECRET;
    private final int EXPIRATION_TIME = 1000 * 60 * 60 * 5; // 5 hr

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 5 hr
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
```

- Create a JwtFilter class

This class will be responsible for taking care of the authentication. All the authenticated request will pass through this filter. This will need the use of UserDetailService class and JwtUtil class.

```java
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwtToken);
        }

        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwtToken)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }
}
```


- In the SecurityConfig class, changes must be made so that the request should pass through the filter, instead of basic auth.

JwtFilter class needs to be injected here.

```java
// These are the changes that need to be done in SecurityConfig class
@Autowired
private JwtFilter jwtFilter;
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/article/**", "/user/**").authenticated()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // this is the change

    return http.build();
}
```






