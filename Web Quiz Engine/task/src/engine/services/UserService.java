package engine.services;

import engine.UserPrincipal;
import engine.exceptions.*;
import engine.models.User;
import engine.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    private static boolean isEmailValid( String email ) {
        final Pattern emailRegex = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",Pattern.CASE_INSENSITIVE);
        return emailRegex.matcher(email).matches();
    }
    public void addUser( User newUser ) {
        if (newUser.getEmail() == null || newUser.getPassword() == null) {
            throw new UserInvalidInputException();
        }

        if (userRepository != null) {
            userRepository.findAll()
                          .stream()
                          .filter(user -> user.getEmail().equals(newUser.getEmail()))
                          .findAny().ifPresent(user -> {
                throw new UserEmailTakenException();
            });

            userRepository.findAll()
                          .stream()
                          .filter(user -> isEmailValid(newUser.getEmail()))
                          .findAny()
                          .orElseThrow(InvalidUserEmailException::new);

            userRepository.findAll()
                          .stream()
                          .filter(user -> newUser.getPassword().length() >= 5)
                          .findAny()
                          .orElseThrow(UserPasswordInvalidLengthException::new);
        }
        String password = newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setPassword(password);
        userRepository.save(newUser);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername( String email ) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new QuizNotFoundException("email");
        }
        return new UserPrincipal(user);
    }


}
