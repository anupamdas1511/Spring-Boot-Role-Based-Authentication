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
                ).httpBasic(Customizer.withDefaults());

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



